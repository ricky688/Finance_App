package hk.vibefinance.nfcprobe;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/** Standalone two-minute RF probe. No wallet role, NFC routing or location APIs. */
public final class ProbeActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView result;
    private Button start;
    private final Runnable refresh = new Runnable() {
        @Override public void run() {
            String saved = getSharedPreferences("probe", MODE_PRIVATE).getString("result", "尚未測試");
            result.setText(saved);
            start.setEnabled(!CaptureService.running);
            handler.postDelayed(this, 1000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        body.setPadding(pad, pad, pad, pad);
        body.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
            view.setPadding(pad + bars.left, pad + bars.top, pad + bars.right, pad + bars.bottom);
            return insets;
        });
        TextView title = new TextView(this);
        title.setText("NFC 拍卡診斷 · 兩分鐘"); title.setTextSize(24);
        body.addView(title);
        TextView info = new TextView(this);
        info.setText("只記錄感應場開／關時間。沒有定位、卡號、付款內容或網絡權限。\n開始後可鎖屏拍真讀卡器；兩分鐘後自動停止。\n「已註冊」不代表已確認能接收事件。");
        info.setTextSize(16); body.addView(info);
        start = new Button(this); start.setText("開始兩分鐘測試");
        start.setMinHeight(Math.round(52 * getResources().getDisplayMetrics().density));
        start.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            } else begin();
        });
        body.addView(start);
        Button stop = new Button(this); stop.setText("停止測試");
        stop.setOnClickListener(view -> stopService(new Intent(this, CaptureService.class)));
        body.addView(stop);
        result = new TextView(this); result.setTextSize(15); result.setTextIsSelectable(true);
        ScrollView scroll = new ScrollView(this); scroll.addView(result);
        body.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(body);
    }
    private void begin() {
        try { startForegroundService(new Intent(this, CaptureService.class)); }
        catch (RuntimeException e) { result.setText("無法啟動：" + e.getClass().getSimpleName()); }
    }
    @Override public void onRequestPermissionsResult(int request, String[] permissions, int[] grants) {
        super.onRequestPermissionsResult(request, permissions, grants);
        if (request == 1 && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) begin();
        else result.setText("未允許通知；測試尚未開始。");
    }
    @Override protected void onResume() { super.onResume(); handler.post(refresh); }
    @Override protected void onPause() { handler.removeCallbacks(refresh); super.onPause(); }

    public static final class CaptureService extends Service {
        static volatile boolean running;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final EventLog log = new EventLog();
        private CardEmulation card;
        private boolean registered;
        private long deadline;
        private String status = "尚未測試";
        private final DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
        private final CardEmulation.NfcEventCallback callback = new CardEmulation.NfcEventCallback() {
            @Override public void onRemoteFieldChanged(boolean on) {
                if (!running) return;
                log.add(time.format(Instant.now()), on);
                status = "已收到 NFC 感應場事件";
                save();
            }
        };
        private final Runnable tick = new Runnable() {
            @Override public void run() {
                if (SystemClock.elapsedRealtime() >= deadline) { status = "兩分鐘結束"; stopSelf(); }
                else { save(); handler.postDelayed(this, 1000); }
            }
        };
        @Override public int onStartCommand(Intent intent, int flags, int id) {
            if (running) return START_NOT_STICKY;
            NotificationManager notifications = getSystemService(NotificationManager.class);
            notifications.createNotificationChannel(new NotificationChannel("capture", "NFC 限時診斷", NotificationManager.IMPORTANCE_LOW));
            PendingIntent open = PendingIntent.getActivity(this, 0, new Intent(this, ProbeActivity.class), PendingIntent.FLAG_IMMUTABLE);
            Notification notification = new Notification.Builder(this, "capture")
                .setSmallIcon(android.R.drawable.ic_menu_info_details).setContentTitle("NFC 拍卡診斷進行中")
                .setContentText("只記錄感應場時間；兩分鐘後自動停止")
                .setContentIntent(open).setOngoing(true).setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE).build();
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE);
            deadline = SystemClock.elapsedRealtime() + 120_000;
            running = true;
            try {
                NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
                if (adapter == null || !adapter.isEnabled()) throw new IllegalStateException("NFC 未開啟");
                card = CardEmulation.getInstance(adapter);
                card.registerNfcEventCallback(getMainExecutor(), callback);
                registered = true;
                status = "已呼叫註冊，等待拍卡（尚未確認能接收）";
                save(); handler.post(tick);
            } catch (RuntimeException e) {
                status = "註冊失敗：" + e.getClass().getSimpleName();
                stopSelf();
            }
            return START_NOT_STICKY;
        }
        private void save() {
            long seconds = running ? Math.max(0, (deadline - SystemClock.elapsedRealtime()) / 1000) : 0;
            String text = status + "\n剩餘 " + seconds + " 秒\n事件數 " + log.size() + "（ON／OFF 各算一筆）\n" + log.text();
            getSharedPreferences("probe", MODE_PRIVATE).edit().putString("result", text).apply();
        }
        @Override public void onDestroy() {
            running = false;
            handler.removeCallbacksAndMessages(null);
            if (registered) {
                try { card.unregisterNfcEventCallback(callback); }
                catch (RuntimeException ignored) { status = "已停止；解除監聽回報錯誤"; }
            }
            if (!status.contains("結束") && !status.contains("失敗") && !status.contains("錯誤")) status = "已停止";
            save(); stopForeground(STOP_FOREGROUND_REMOVE);
            super.onDestroy();
        }
        @Override public void onTimeout(int startId) { status = "系統時限已到"; stopSelf(); }
        @Override public void onTimeout(int startId, int type) { status = "系統時限已到"; stopSelf(); }
        @Override public IBinder onBind(Intent intent) { return null; }
    }
}
