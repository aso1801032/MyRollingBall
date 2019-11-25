package jp.ac.myrollingball

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener, SurfaceHolder.Callback {

    // プロパティ
    private var surfaceWidth:Int = 0; // サーフェスの幅
    private var surfaceHeight:Int = 0; // サーフェスの高さ

    private val radius = 40.0f; // ボールの半径
    private val coef = 1000.0f; // ボールの移動量を計算するための係数（計数）

    private var ballX:Float = 0f; // ボールの現在のX座標
    private var ballY:Float = 0f; // ボールの現在のY座標
    private var vx:Float = 0f; // ボールのX方向の加速度
    private var vy:Float = 0f; // ボールのY方向の加速度
    private var time:Long = 0L; // 前回の取得時間

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val holder = surfaceView.holder; // サーフェスホルダーを取得
        // サーフェスホルダーのコールバックに自クラスを追加
        holder.addCallback(this);
        // 画面の縦横指定をアプリから指定してロック(縦方向に指定)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        resetbuton.setOnClickListener{ onresetbuttonTappaed(surfaceView) }
    }

    //画面表示・再表示のイベントコールバックメソッド
    override fun onResume() {
        super.onResume()
    }

    //画面が非表示になるたびに呼ばれるコールバックメソッド
    override fun onPause() {
        super.onPause()


    }

    //センサーの構成？が変更されると通知される
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event == null){return}

        //センサーが変わった時にボールを描画するための情報を計算する
        //一番最初のセンサー検知の時の初期時間を取得
        if(time == 0L){
            time = System.currentTimeMillis()
        }
        //eventのセンサー種別が加速度センサーだったら以下を実行
        if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //センサーが取得した値を変数に代入
            val x = event.values[0]*-1;
            val y = event.values[1];

            //前回の時間からの経過時間を計算(今の時間ー前回の時間＝経過時間)
            var t = (System.currentTimeMillis()-time).toFloat()

            //timeに今のじかんを前の時間として保存し直す
            time = System.currentTimeMillis();
            t /= 1000.0f;//ms単位をびょう単位に直すため1000でわる

            //移動距離を計算
            val dx = (vx*t)+(x*t*t)/2.0f;//移動すべききょり
            val dy = (vy*t)+(y*t*t)/2.0f;
            this.ballX += (dx*coef);//ボールの新しい座標
            this.ballY += (dy*coef);
            //今時間の加速度を保存し直す
            this.vx += (x*t);
            this.vy += (y*t);

            // 画面の端にきたら跳ね返る処理
            // 左右について
            if( (ballX -radius)<0 && vx<0 ){
                // 左にぶつかった時
                vx = -vx /1.5f;
                ballX = radius;
            }else if( (ballX+radius)>surfaceWidth && vx>0){
                // 右にぶつかった時
                vx = -vx/1.5f;
                ballX = (surfaceWidth-radius);
            }
            // 上下について
            if( (ballY -radius)<0 && vy<0 ){
                // 下にぶつかった時
                vy = -vy /1.5f;
                ballY = radius;
            }else if( (ballY+radius)>surfaceHeight && vy>0 ){
                // 上にぶつかった時
                vy = -vy/1.5f;
                ballY = surfaceHeight -radius;
            }

            //r1
            if( (ballX -radius)>150f && (ballY +radius)>500f && (ballX +radius)<650f && (ballY -radius)<550f ){
                textView.setText("失敗！")
                ballX = 500f;
                ballY = 100f;
                imageView.setImageResource(R.drawable.oti)
            }
            //r2
            if( (ballX -radius)>300f && (ballY +radius)>950f && (ballX +radius)<1000f && (ballY -radius)<1000f ){
                textView.setText("失敗！")
                ballX = 500f;
                ballY = 100f;
                imageView.setImageResource(R.drawable.oti)
            }
            //goal
            if( (ballX -radius)>450f && (ballY +radius)>1200f && (ballX +radius)<550f && (ballY -radius)<1400f ){
                textView.setText("成功！")
                ballX = 500f;
                ballY = 100f;
                imageView.setImageResource(R.drawable.egao)
            }

            //キャンパスに描画
            this.drawCanvas();

        }

        //センサーの値が変わった時の処理を書く
        Log.d("TAG01","センサーの値が変わりました")
        //引数の中身が何もなかったら何もせずに終了
        if(event == null){return}
        //加速度センサーのイベントか判定
        /*if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
            //ログに表示するための文字数を組み立てる
            var str:String = "x方向の値:${event.values[0].toString()}"+
                    "y方向の値:${event.values[1].toString()}"+
                    "z方向の値:${event.values[2].toString()}";
            //デバッグログに出力
            Log.d("加速度センサー",str)
        }*/
    }

    //サーフェーすが更新されるイベントに反応して呼ばれるコールバックメソッド
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        //サーフェーすが変化するたびに幅と高さを設定インスタンスに記憶しておく
        this.surfaceHeight = height;
        this.surfaceWidth = width;
        // ボールの初期位置を保存しておく
        ballX = (width/2).toFloat();
        ballY = (height/5).toFloat();

    }

    //サーフェーすが破棄された時のイベントに反応して呼ばれるコールバックメソッド
    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        //センサーマネージャーのインスタンス生成
        val sensorManager=this.getSystemService(Context.SENSOR_SERVICE)as SensorManager;
        //センサーマネージャーから登録した次クラスの解除
        sensorManager.unregisterListener(this)
    }

    //サーフェーすが生成されたときのイベントに反応して呼ばれるコールバックメソッド
    override fun surfaceCreated(p0: SurfaceHolder?) {
        //センサーマネージャーのインスタンスをOSから取得
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE)as SensorManager;
        //センサーマネージャーから加速度センサーを指定してインスタンスを取得
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //センサーのリスナーに登録して加速度センサーの監視を開始
        sensorManager.registerListener(
            this,//イベントリスナー機能を持つインスタンス。今回は画面クラス、ここに通知
            accSensor,//監視するセンサーのインスタンス。加速度センサー
            SensorManager.SENSOR_DELAY_GAME//センサーの更新通知頻度
        )    }

    private fun drawCanvas(){

        val canvas = surfaceView.holder.lockCanvas();

        // キャンバスの背景色を設定
        canvas.drawColor(Color.DKGRAY);

        // キャンバスに円を描いてボールにする
        canvas.drawCircle(
            ballX, // ボール中心のX座標
            ballY, // ボール中心のY座標
            radius, // 半径
            Paint().apply {
                color = Color.RED; } // ペイントブラシのインスタンス
        );
        //r1
        canvas.drawRect(
            150f,500f,650f,550f,Paint().apply { color = Color.BLACK; }
        );
        //r2
        canvas.drawRect(
            300f,950f,1000f,1000f,Paint().apply { color = Color.BLACK; }
        );
        //goal
        canvas.drawRect(
            450f,1200f,550f,1400f,Paint().apply { color = Color.RED; }
        );
        // キャンバスをアンロック（ロック解除）してキャンバスを描画(ポスト)
        surfaceView.holder.unlockCanvasAndPost(canvas);
    }

    fun onresetbuttonTappaed(view: View){
        ballX = 500f;
        ballY = 100f;
        imageView.setImageResource(R.drawable.ouen)
        textView.setText("がんばれ！")
    }


}
