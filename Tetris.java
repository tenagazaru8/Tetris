package Tetris;

//必要なものインポート
import java.awt.Color; //色を扱うため
import java.awt.Font; //フォント指定のため
import java.awt.Graphics; //図形の描画を行うため
import java.awt.event.KeyEvent; //キーの入力を受け取るため
import java.awt.event.KeyListener; //キーの状態を読み取るため
import java.util.ArrayList; //ArrayListを使用するため
import java.util.Random; //乱数を使用するため

import javax.swing.JFrame; //Frameを使用するため
import javax.swing.JPanel; //Panelを使用するため

//メインのクラス
public class Tetris {
	static private JPanel panel;
	public static void main(String[] args) {
		//ウィンドウの生成
		GameWindow gw = new GameWindow("テ○リス");
		
		//ホーム画面の設定
		panel = new HomeCanvas();
		//ホーム画面への切り替え
		gw.change(panel);
		panel.requestFocus();
		//ホーム画面の処理
		gw.startHomeLoop();
		
		//ゲーム画面の設定
		panel = new GameCanvas();
		//ゲームの画面へ切り替え
		gw.change(panel);
		panel.requestFocus();
		//ゲームのループをスタート
		gw.startGameLoop();
	}
}

//ウィンドウの基本的な処理を含むクラス
class GameWindow extends JFrame implements Runnable {
	//マルチスレッドを使用
	private Thread th = null;
	//画面サイズの定数を定義
	static private final int WIDTH = 1000, HEIGHT = 900;
	
	//コンストラクタ
	public GameWindow(String title) {
		super(title);
		
		//×でウィンドウを閉じるように設定
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		//ウィンドウのサイズを設定
		setSize(WIDTH, HEIGHT);
		//ウィンドウを画面の中心に表示するように設定
		setLocationRelativeTo(null);
		//ウィンドウのサイズを変更できないように設定
		setResizable(false);
		
		//ウィンドウを可視化
		setVisible(true);
	}
	
	//画面切り替え用メソッド
	public void change(JPanel panel) {
		//ContentPaneにはめ込まれたパネルを削除
		getContentPane().removeAll();
		
		//パネルの追加
		super.add(panel);
		//更新
		validate();
		//描画
		repaint();
	}
	
	//ホーム画面でのループの処理
	public void startHomeLoop() {
		//Homeのフラグがtrueの間ループ
		while (HomeCanvas.getFlag()) {
			//再描画
			repaint();
			
			//25fpsでループ
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//ゲームループの開始メソッド
	public synchronized void startGameLoop(){
		if ( th == null ) {
			th = new Thread(this);
			th.start();
			try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//ゲームループの終了メソッド
	public synchronized void stopGameLoop(){
		if ( th != null ) {
			th = null;
		}
	}
	
	//ゲームの処理
	public void run() {
		//経過時間を記録するための変数
		int time = 0, count = 0;
		
		while (th != null) {
			//再描画
			repaint();
			
			//テトリミノ(TM)が落下中かどうかで条件分岐、trueなら落下中
			if (Field.getFall()) {
				//段々短くなる周期ごとにTMを落下させる
				if (time++ > 5 + 21000 / (count + 600)) {
					Field.FallTM();
					
					time = 0;
					++count;
				}
			} else {
				//そろっている行の削除とスコアの更新
				TotallingLine.Totalling(Field.delLine());
				
				//Holdできるようにリセット
				Hold.resetFlag();
				
				//次のTMを初期位置に設定
				if (!Field.setFieldTM(Next.getNext())) {
					//失敗したらゲームオーバーの処理
					stopGameLoop();
					break;
				}
				
				//Nextを更新
				Next.updateNext();
			}	
			
			//50fpsに調節
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

//ホーム画面の描画に関するクラス
class HomeCanvas extends JPanel implements KeyListener {
	//ホーム画面のフラグ
	static private boolean flag;
	//点滅表示のために使うループカウント変数
	static private int time;
	//フォントを指定するための変数
	static private Font TitleFont, DirectionFont;
	static private final int X = 350, Y = 200;
	
	//各変数の初期化処理
	public HomeCanvas() {
		//キー入力を読み取るように設定
		addKeyListener(this);
		
		flag = true;
		time = 0;
		
		//フォントの設定
		TitleFont = new Font("Arial", Font.BOLD, 70);
		DirectionFont = new Font("HGP創英角ﾎﾟｯﾌﾟ体", Font.BOLD, 30);
		
	}
	
	//flagのゲッター
	public static boolean getFlag() {
		return flag;
	}
	
	//キーがタイプ、押されたときの処理
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyPressed(KeyEvent e) {}
 
	//キーが離されたときの処理
	@Override
	public void keyReleased(KeyEvent e) {
		switch ( e.getKeyCode() ) {
		case KeyEvent.VK_SPACE:
			//スペースキー, ホームからゲーム画面への切り替え
			flag = false;
			break;
		}
	}
	
	//描画メソッド
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//タイトルのフォントの指定と描画
		g.setFont(TitleFont);
		g.drawString("TEORIS", X, Y);
		
		//操作方法のフォント指定と描画
		g.setFont(DirectionFont);
		g.drawString("矢印キー･･･移動", X + 10, Y + 150);
		g.drawString("Qキー･･･左回転", X + 15, Y + 200);
		g.drawString("Eキー･･･右回転", X + 15, Y + 250);
		g.drawString("Wキー･･･ホールド", X + 5, Y + 300);
		
		if (++time >= 20)
			//指示の点滅表示
			g.drawString("スペースキーでスタート", X - 20, Y + 450);
		
		time %= 40;
	}
}

//ゲーム画面の描画に関するクラス
class GameCanvas extends JPanel implements KeyListener {
	//描画オブジェクトを記録するための変数(ArrayList)
	static private ArrayList<GObject> object = new ArrayList<GObject> ();
	
	//各変数の設定
	public GameCanvas() {
		//キー入力を読み取るように設定
		addKeyListener(this);
		
		object.add(Field.getInstance());
		object.add(Next.getInstance());
		object.add(Hold.getInstance());
		object.add(TotallingLine.getInstance());
	}

	//キーがタイプされたときの処理
	@Override
	public void keyTyped(KeyEvent e) {}
 
	//キーが押されたときの処理
	@Override
	public void keyPressed(KeyEvent e) {		
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			//↑キー, ハードドロップの処理
			Field.HardDropTM();
			//再描画
			repaint();
			break;
		case KeyEvent.VK_LEFT:
			//←キー, テトリミノを左に移動
			Field.MoveLeftTM();
			break;
		case KeyEvent.VK_RIGHT:
			//→キー, テトリミノを右に移動
			Field.MoveRightTM();
			break;
		case KeyEvent.VK_DOWN:
			//↓キー, テトリミノを下に移動
			Field.MoveDownTM();
			break;
		case KeyEvent.VK_Q:
			//Qキー, テトリミノを左回転
			Field.LeftRot();
			break;
		case KeyEvent.VK_W:
			//Wキー, テトリミノのホールド
			Field.HoldTM();
			break;
		case KeyEvent.VK_E:
			//Eキー, テトリミノを右回転
			Field.RightRot();
			break;
		}
	}
 
	//キーが離されたときの処理
	@Override
	public void keyReleased(KeyEvent e) {}
	
	//描画メソッド
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//各描画オブジェクトの描画
		for (GObject obj: object)
			obj.draw(g);
	}
}

//描画オブジェクトを同一クラスとして扱うための抽象クラス
abstract class GObject {
	//ミノのサイズを共通の定数として設定
	static protected final int SIZE = 35;
	
	//drawメソッド
	public void draw(Graphics g) {}
}

//枠内にTMを表示するクラス
class TMDisplay extends GObject {
	//表示のために使うmino
	private Mino mino;
	//TMの情報を保持するための変数
	private TetriMino TM;
	//描画の基準となる座標を記録する変数
	private int X, Y;
	
	//コンストラクタ
	public TMDisplay(int xx, int yy) {
		mino = new Mino();
		TM = new TetriMino();
		X = xx;
		Y = yy;
	}
	
	//表示するTMの番号のゲッター
	public int getTMid() {
		return TM.getTMid();
	}
	
	//表示するTMの設定
	public void setTM(int TMid) {
		TM.setTM(TMid);
		mino.setvisible(true);
		mino.setcolor(TM.getcolor());
	}
	
	//描画メソッド
	public void draw(Graphics g) {
		//背景の描画
		g.setColor(Color.darkGray);
		g.fillRect(X, Y, SIZE * 5, SIZE * 5);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(X + SIZE / 3, Y + SIZE / 3, SIZE * 13 / 3, SIZE * 13 / 3);
		
		//TMの番号が-1ならば表示しない
		if (TM.getTMid() == -1)
			return;
		
		//TMの表示
		for (int i = 0; i < 4; ++i)
			mino.draw(g, X + (4 - TM.getcx() + TM.getx(i)) * SIZE / 2, Y + (TM.gety(i) + 1) * SIZE / 2);
	}
}

//登場する予定のミノを扱うクラス
class Next extends GObject{
	//TMを表示するための配列と配列の大きさ
	static private TMDisplay[] next;
	static private final int NEXTNUM = 4;
	//乱数を生成するためのインスタンス
	static private Random rand;
	//描画の基準となる座標を記録する変数
	static private int  X, Y;
	//フォントを指定するための変数
	static private Font font;
	
	//唯一のインスタンス
	static private Next instance = new Next(750, 50);
	
	//privateでコンストラクタを定義
	private Next(int xx, int yy) {
		//初期化処理
		next = new TMDisplay[NEXTNUM];
		rand = new Random();
		X = xx;
		Y = yy;
		font = new Font("Arial", Font.BOLD, 50);
		
		//各TMDisplayの座標の決定と最初に表示するTMの決定
		for (int i = 0; i < NEXTNUM; ++i) {
			next[i] = new TMDisplay(X, Y + i * SIZE * 14 / 3 + SIZE);
			
			next[i].setTM(rand.nextInt(7));
		}
	}
	
	//インスタンスのゲッター
	public static Next getInstance() {
		return instance;
	}
	
	//次のTMの番号のゲッター
	public static int getNext() {
		return next[0].getTMid();
	}
	
	//TMのリストを更新して新しいTMを決定
	public static void updateNext() {
		for (int i = 1; i < NEXTNUM; ++i)
			next[i - 1].setTM(next[i].getTMid());
		
		next[NEXTNUM - 1].setTM(rand.nextInt(7));
	}
	
	//描画メソッド
	public void draw(Graphics g) {
		//各TMDisplayの描画
		for (int i = 0; i < NEXTNUM; ++i)
			next[i].draw(g);
		
		//NEXTの描画
		g.setFont(font);
		g.setColor(Color.black);
		g.drawString("NEXT", X + 20, Y + SIZE - 10);
	}
}

//一時的に確保するミノを扱うクラス
class Hold extends GObject {
	//TMを表示するための変数
	static private TMDisplay hold;
	//描画の基準となる座標を記録する変数
	static private int  X, Y;
	//すでにホールドの処理をしたかどうかのflag
	static private boolean flag;
	static private Font font;
	//このクラス唯一のインスタンス
	static private Hold instance = new Hold(50, 50);
	
	//privateでコンストラクタを定義
	private Hold(int xx, int yy) {
		//初期化処理
		X = xx;
		Y = yy;
		flag = false;
		hold = new TMDisplay(X, Y + SIZE);
		font = new Font("Arial", Font.BOLD, 50);
	}
	
	//インスタンスのゲッター
	public static Hold getInstance() {
		return instance;
	}
	
	//ホールドの入れ替え処理
	public static int swapHold(int TMid) {
		int currentid = hold.getTMid();
		flag = true;
		
		hold.setTM(TMid);
		
		return currentid;
	}
	
	//flagのゲッター
	public static boolean getflag() {
		return flag;
	}
	
	//flagのリセット
	public static void resetFlag() {
		flag = false;
	}
	
	//描画メソッド
	public void draw(Graphics g) {
		hold.draw(g);
		
		//HOLDの描画
		g.setFont(font);
		g.setColor(Color.black);
		g.drawString("HOLD", X + 20, Y + SIZE - 10);
	}
}

//ミノを操作する画面のクラス
class Field extends GObject {
	//ミノの2次元配列と各定数を宣言
	static private Mino [][] field;
	//テトリミノの原点を記録する変数
	static private TetriMino TM;
	//TMの扱いに関する変数
	static private int tx, ty, WX, WY, index, tempxy[][], dellinenum[];
	//TMが落下中かどうかを記録する変数
	static private boolean fall;
	//描画に関する定数の設定
	static private final int
			XLEN = 10,                     //フィールドに入るミノの列数
			YLEN = 22,                     //フィールドに入るミノの行数
			WIDTH = SIZE * XLEN,          //横幅
			HEIGHT = SIZE * YLEN,         //縦幅
			THICK = 15;                   //枠線の太さ
	
	//このクラス唯一のインスタンス
	static private Field instance = new Field(330, 30);
	
	//privateでコンストラクタを定義
	private Field(int xx, int yy) {
		//フィールドの四方を囲むようにミノの2次元配列の確保
		field = new Mino [YLEN + 5][XLEN + 2];
		TM = new TetriMino();
		//フィールドの座標を決定
		WX = xx;
		WY = yy;
		index = 0;
		tempxy = new int[2][4];
		dellinenum = new int[4];
		fall = false;
		
		int i, j;
		
		//配列の要素に対応するミノを生成
		for (i = 0; i < YLEN + 5; ++i)
		for (j = 0; j < XLEN + 2; ++j)
			field[i][j] = new Mino();
		
		//枠の部分を指定
		for (i = 0; i < YLEN + 5; ++i) {
			field[i][0].setvisible(true);
			field[i][XLEN + 1].setvisible(true);
		}
		for (i = 1; i < XLEN + 1; ++i)
			field[YLEN + 4][i].setvisible(true);
	}
	
	//インスタンスのゲッター
	public static Field getInstance() {
		return instance;
	}
	
	//fallのゲッターとセッター
	public static boolean getFall() {
		return fall;
	}
	
	public static void setFall() {
		fall = false;
	}
	
	//FieldにTMを生成する、成功、失敗でtrue, falseを返す
	public static boolean setFieldTM(int TMid) {	
		//TMの設定
		TM.setTM(TMid);
		
		//生成する座標
		tx = 4;
		ty = 3;
		
		int i;
		
		//生成する位置にミノが存在していたらfalseを返す
		for (i = 0; i < 4; ++i)
			if (field[ty + TM.gety(i) / 2][tx + TM.getx(i) / 2].getvisible())
				return false;
		
		//TMの色のコピーと可視化
		setTMcolor();
		setTMvisible(true);
		
		//
		fall = true;
			
		return true;
	}
	
	//そろっている行の削除、削除した行数を返す
	public static int delLine() {
		int i, j;
		boolean line;
		
		//削除の処理
		for (i = YLEN + 3, index = 0; i > 0; --i) {
			for (j = 1, line = true; j <= XLEN; ++j) {
				//行で揃っているかの確認
				if (!field[i][j].getvisible())
					line = false;
				
				//フィールドの更新
				field[i + index][j].setcolor(field[i][j].getcolor());
				field[i + index][j].setvisible(field[i][j].getvisible());
			}
			
			//行が揃っているなら削除する行数を増やす
			if (line)
				dellinenum[index++] = i;
		} 
		
		return index;
	}
	
	//座標(x, y)のミノの存在を論理値で返す
	private static boolean getMinoVisible(int x, int y) {
		boolean flag;
		
		if (tx + x > XLEN || tx + x < 1 || ty + y > YLEN + 3)
			flag = true;
		else
			flag = field[ty + y][tx + x].getvisible();
		
		return flag;
	}
	
	//TMの可視性を決定
	private static void setTMvisible(boolean v) {
		for (int i = 0; i < 4; ++i)
			field[ty + TM.gety(i) / 2][tx + TM.getx(i) / 2].setvisible(v);
	}
	
	//TMの色をコピー
	private static void setTMcolor() {
		for (int i= 0; i < 4; ++i)
			field[ty + TM.gety(i) / 2][tx + TM.getx(i) / 2].setcolor(TM.getcolor());
	}
	
	//n=1で右回転, n=-1で左回転の処理を行うprivateメソッド, 回転できない場合は何もしない
	private static void RotTM(int n) {
		int i, j, k, temp = 0;
		boolean flag = true;
		
		//回転した後の座標を格納
		for (i = 0; i < 4; ++i) {
			tempxy[0][i] = n * TM.getcy() + TM.getcx() - n * TM.gety(i);
			tempxy[1][i] = TM.getcy() - n * TM.getcx() + n * TM.getx(i);
		}
		
		//TMの不可視化
		setTMvisible(false);
		
		for (i = 1; i <= 3; ++i) {
			for (j = 0; j <= 4; ++j) {
				//距離が短い位置から移動できる座標を探索するための計算
				temp = (j * (3 * j + 2 * j * j - 1) + 2) % 5 - 2;
				
				//TMが既存のミノと重なるかの確認
				for (k = 0, flag = true; k < 4; ++k)
					if (getMinoVisible(tempxy[0][k] / 2 + temp, tempxy[1][k] / 2 + i % 3 - 1))
						flag = false;
				
				if (flag)
					break;
			}
			if (flag)
				break;
		}
		
		//既存のミノと重ならないならTMの位置を変更
		if (flag) {
			//TMの基点を変更
			tx += temp;
			ty += i % 3 - 1;
			
			//TMの各ミノの位置を変更
			for (i = 0; i < 4; ++i) {
				TM.setx(i, tempxy[0][i]);
				TM.sety(i, tempxy[1][i]);
			}
			//ミノの色をコピー
			setTMcolor();
		}
		
		//TMを可視化
		setTMvisible(true);
	}
	
	//テトリミノを右に90度回転
	public static void RightRot() {
		RotTM(1);
	}

	//テトリミノを左に90度回転
	public static void LeftRot() {
		RotTM(-1);
	}
	
	//ミノを動かす
	public static boolean MoveTM(int xx, int yy) {
		int i;
		boolean line;
		
		//TMを不可視化
		setTMvisible(false);
		
		//TMを移動した先で既存のミノと重なるかどうかを確認
		for (i = 0, line = true; i < 4; ++i)
			if (getMinoVisible(TM.getx(i) / 2 + xx, TM.gety(i) / 2 + yy))
				line = false;
		
		//重ならないならTMを移動
		if (line) {
			tx += xx;
			ty += yy;
		}
		
		//TMの可視化と色のコピー
		setTMcolor();
		setTMvisible(true);
		
		return line;
	}
	
	//TMを右に動かす
	public static void MoveRightTM() {
		MoveTM(1, 0);
	}
	
	//TMを左に動かす
	public static void MoveLeftTM() {
		MoveTM(-1, 0);
	}
	
	//TMを下に動かす
	public static void MoveDownTM() {
		MoveTM(0, 1);
	}
	
	//TMのハードドロップ
	public static void HardDropTM() {
		while (MoveTM(0, 1));
		
		fall = false;
	}
	
	//TMの落下処理
	public static void FallTM() {
		if (MoveTM(0, 1))
			return;
		
		fall = false;
	}
	
	//TMのホールド
	public static void HoldTM() {
		//既にホールドをした場合は何もしない
		if (Hold.getflag())
			return;
		
		//ホールドしているTMの番号を記録する
		int TMid = Hold.swapHold(TM.getTMid());
		
		//TMを不可視化
		setTMvisible(false);
		
		//TMの番号が-1ならば次のTMを指定する
		if (TMid == -1) {
			fall = false;
			return;
		}
		
		//TMを更新
		setFieldTM(TMid);
	}

	//描画メソッド
	public void draw(Graphics g) {
		int i, j;
		
		//背景の描画
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(WX, WY, WIDTH, HEIGHT + THICK);
		
		//
		g.setColor(Color.gray);
		for (i = 1; i < XLEN; ++i)
			g.drawLine(WX + i * SIZE, WY, WX + i * SIZE, WY + HEIGHT);
		
		//枠線の描画
		g.setColor(Color.black);
		g.fillRect(WX - THICK, WY, THICK, HEIGHT + THICK);
		g.fillRect(WX + WIDTH, WY, THICK, HEIGHT + THICK);
		g.fillRect(WX, WY + HEIGHT, WIDTH, THICK);
		
		
		//配列に格納されたミノを描画
		for (i = 3; i < YLEN + 4; ++i)
		for (j = 1; j < XLEN + 1; ++j)
			field[i][j].draw(g, WX + SIZE * (j - 1), WY + SIZE * (i - 4));
	}
}

//スコアと揃った行数の管理を行うクラス
class TotallingLine extends GObject {
	//スコア、削除した行数、描画の基準点を記録する変数
	private static int count, score, totalline, WX, WY;
	private static Font font;
	
	//このクラス唯一のインスタンス
	private static TotallingLine instance = new TotallingLine(50, 600);
	
	//コンストラクタをprivateで宣言
	private TotallingLine(int xx, int yy) {
		count = 0;
		score = 0;
		totalline = 0;
		WX = xx;
		WY = yy;
		font = new Font("Arial", Font.BOLD, 50);
	}
	
	//インスタンスのゲッター
	public static TotallingLine getInstance() {
		return instance;
	}
	
	//スコア、行数の更新
	public static void Totalling(int delline) {
		//削除した行数が0の場合は連続ボーナスを0にする
		if (delline <= 0) {
			count = 0;
			return;
		}
	
		//スコアの計算と削除した行数の更新
		score += (100 + 10 * count) * delline;
		totalline += delline;
		count++;
	}
	
	//描画メソッド
	public void draw(Graphics g) {
		//フォントの指定
		g.setFont(font);

		//枠と背景の描画
		g.setColor(Color.black);
		g.fillRect(WX - 15, WY - 50, 220, 220);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(WX - 5, WY- 40, 200, 200);
		g.setColor(Color.black);
		g.fillRect(WX - 5, WY + 55, 200, 6);
		
		//スコアの描画
		g.drawString("SCORE", WX, WY);
		g.drawString(Integer.toString(score), WX - g.getFontMetrics().stringWidth(Integer.toString(score)) + 180, WY + 50);
		
		//削除した行数の描画
		g.drawString("LINE", WX, WY + 100);
		g.drawString(Integer.toString(totalline), WX - g.getFontMetrics().stringWidth(Integer.toString(totalline)) + 180, WY + 150);
	}
}

//TMの情報を扱うクラス
class TetriMino {
	//ゲームに登場するテトリミノ(TM)に関する情報を設定
	private static final char minopoint[] = {
			0b111100000000,   //  I
			0b011001100000,   //  O
			0b111001000000,   //  T
			0b011011000000,   //  S
			0b110001100000,   //  Z
			0b111000100000,   //  J
			0b111010000000,   //  L
			};
	private static final Color minocolor[] = {
			Color.decode("0x66ccff"), //  空色
			Color.decode("0xfaf500"), //  黄色
			Color.decode("0xff00ff"), //  明るい紫
			Color.decode("0x00e700"), //  明るい緑
			Color.decode("0xff4500"), //  赤色
			Color.decode("0x0041ff"), //  青色  
			Color.decode("0xff9900"), //  オレンジ
	        };
	
	//TMの座標、回転軸、番号を表す変数
	private int x[], y[], cx, cy, TMid;
	
	//コンストラクタ
	public TetriMino() {
		x = new int[4];
		y = new int[4];
		TMid = -1;
	}
	
	//TMに含まれるミノの座標のゲッター
	public int getx(int index) {
		try {
			return x[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Should be an integer between 0 and 3");
			System.exit(1);
			return -1;
		}
	}
	public int gety(int index) {
		try {
			return y[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Should be an integer between 0 and 3");
			System.exit(1);
			return -1;
		}
	}
	
	//TMに含まれるミノの座標のセッター
	public void setx(int index, int xx) {
		try {
			 x[index] = xx;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Should be an integer between 0 and 3");
		}
	}
	public void sety(int index, int yy) {
		try {
			y[index] = yy;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Should be an integer between 0 and 3");
		}
	}
	
	//TMの中心座標のゲッター
	public int getcx() {
		return cx;
	}
	public int getcy() {
		return cy;
	}
	
	//TMの番号を返す
	public int getTMid() {
		return TMid;
	}
	
	//TMの色を返す
	public Color getcolor() {
		return minocolor[TMid];
	}
	
	//指定した番号のテトリミノをフィールドに設定
	public void setTM(int n) {
		int i, j, temp;
		
		//配列を使うためのエラー処理
		if (n < 0 || 6 < n) {
			System.err.println("Should be an integer between 0 and 6");
			System.exit(2);
		}
		
		//TMの番号の設定
		TMid = n;
		//回転軸の設定
		cx = cy = (n > 1 ? 4 : 3);
		//ミノの配置の設定
		for (temp = minopoint[n], i = j = 0; j < 4; ++i, temp >>= 1) {
			if ((temp & 1) == 1) {
				x[j] = (i % 4) * 2;
				y[j++] = (i / 4) * 2;
			}
		}
	}
}

//ミノを扱うクラス
class Mino extends GObject {
	//ミノの存在、色に関するフィールド
	private boolean visible;
	private Color c;
	
	//ミノのコンストラクタ
	public Mino() {
		c = Color.green;
		visible = false;
	}

	//各変数のセッター
	public void setvisible(boolean v) {
		visible = v;
	}

	public void setcolor(Color cc) {
		c = cc;
	}

	//各変数のゲッター
	public boolean getvisible() {
		return visible;
	}
	
	public Color getcolor() {
		return c;
	}
	
	//ミノの描画メソッド
	public void draw(Graphics g, int x, int y) {
		//不可視の場合何もしない
		if (!visible)
			return;
		
		//色の指定と描画
		g.setColor(c);
		g.fillRect(x, y, SIZE, SIZE);
		
		g.setColor(c.darker());
		g.fillRect(x + SIZE / 5, y + SIZE / 5, SIZE * 3 / 5, SIZE * 3 / 5);
		
		g.setColor(Color.black);
		g.drawRect(x, y, SIZE, SIZE);
	}
}
