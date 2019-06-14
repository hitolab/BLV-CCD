package main;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import java.awt.* ;
import java.awt.event.* ;

import javax.swing.*;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;



import button.*;
import rs232c.*;

public class BLV extends JFrame {

	
	/**
	 * ver3.61 ノイズの時間間隔を分単位で.txtから自由に読めるように変更
	 * ver3.62 DDDDLDもスタートのディレイがhourで設定可能に。hourは小数点も可
	 * ver3.63 DDDDDLボタンを正常な動作をするように変更した。
	 * ver3.64 L,D,LD,DDDDDL,Noise で電流量のログを種類の情報もつけて日時付で残すように変更。
	 * ver3.65 過去のデータを見るボタン←→ボタンを有効にした。
	 * ver3.66 ファイル名ボックスを作る。
	 * ver3.68 CCDカメラをBLVから制御可能に。画像取得の頻度を上部の欄に入力することで指定可能。
	 * ver3.69 あらゆる状況でCCDカメラから撮影可能に。L,D,LD,DDDDDL,NOISEで撮影時はDになり、撮影が終わると本来あるべき明るさへ。
	 * ver3.70machineNoを消去し、グラフをひとつに
	 */
	private static final long serialVersionUID = -2508829707600345047L;
	
	//Parameter
	public static String version = "3.70";// 2019年6月14日
//	public static String curDir = getCurDir();
//	public static String curDir = "C:\\Documents and Settings\\hito\\workspace\\PanelTest";
	public static String curDir;
	public static int SamplingTime = 3; // sampling every 3.0[s]
	public int MACHINENO=0;
	
	public JPanel panel;
	protected GridBagConstraints constraints;	
//	public int expno1;
	GridBagLayout gridbag;
	public CommunicatorPhotomul cp1;
	public CommunicatorT10 ct;
	public CommunicatorKIKUSUI ck;
	public CommunicatorArduino ar;
	
	public DisplayEXPNO cEXP1;
	public myMeasureButton cmb1;
	public BasePlace cbase1,cbase1_1,spacer1,cbase5_0,cbase5_1,cbase5,spacer5;
	public ChartPanel cchart1,cchart5;
	public myStopButton csb1;
	public myPrintButton cpb1;
	public DataUpButton dub1;
	public myButton cb1;
	public MyLightButton lonb;
	public MyLightOFFButton loffb;
	public MyLightNoiseButton lnb;
	public CurrentTextBox ctb;
	public DelayLDTimeTextBox dlttb;
	public MyLDButton ldb;
	public DisplayLDTime ldt;
	public DataDownButton ddb1;
	public GoToMeasuringChart gtmc1;
	public SampleIntervalTimeTextBox sittb;
	public SampleMeasureTimeTextBox smttb;
	public FilenameTextBox ftb;
	public MyDDDDDLButton ddddldb;
	public MyCCDButton arb;
	public IntervalCCDTextBox ictb;
	public ConfirmState cs;
	public LDCycleCounterTextBox ldcctb;
	
	XYSeries dataseries1 = new XYSeries("1"),
	dataseries2 = new XYSeries("2"),
	dataseries3 = new XYSeries("3"),
	dataseries4 = new XYSeries("4"),
	dataseries5 = new XYSeries("light");
	XYSeriesCollection dataset1 = new XYSeriesCollection(),
			dataset2 = new XYSeriesCollection(),
			dataset3 = new XYSeriesCollection(),
			dataset4 = new XYSeriesCollection(),
			dataset5 = new XYSeriesCollection();
	JFreeChart chart1,chart2,chart3,chart4,chart5;
	long startTime1=0, startTime2=0, startTime3=0, startTime4=0;
	public DisplayNo displayNo1,displayNo2,displayNo3,displayNo4;
	
	boolean isWindows = false;
	
	
	public BLV(){
		
		osCheck();
//		setMachineNo();
		
		//シリアルポートの準備
		this.cp1 = new CommunicatorPhotomul(1,dataseries1);//comportを指定
		if(isWindows)this.ck = new CommunicatorKIKUSUI(8); //KIKUSUIとの接続 3は危険。
		else this.ck = new CommunicatorKIKUSUI("/dev/cu.UC-232AC");
		if(isWindows)this.ar = new CommunicatorArduino(5); //Arduinoとの接続、COM=5
		//else this.ar = new CommunicatorArduino("/dev/cu.UC-232AC");
		
		
		//Chartを作る。
		setDatasets();
		chart1 = createScatterPlot(dataset1,"Biolumi. [cnt]");
		chart5 = createStepPlot(dataset5,"LED current [A]");
		setChartPanel();

		//Panelの作成 
		gridbag = new GridBagLayout();
		constraints = new GridBagConstraints();	
		panel = new JPanel();
		panel.setLayout(gridbag);
		
		//Button in first line
		
		cEXP1= new DisplayEXPNO(1);
		sittb = new SampleIntervalTimeTextBox();
		smttb = new SampleMeasureTimeTextBox();
		ictb = new IntervalCCDTextBox();
		ftb = new FilenameTextBox();
		cs = new ConfirmState();
		arb = new MyCCDButton(ictb, ck, cs);
		cmb1= new myMeasureButton(cEXP1,cp1,cchart1,dataseries1 ,1,sittb,smttb,this.MACHINENO,ftb);
		cbase1 = new BasePlace();
		csb1 = new myStopButton(cmb1,this,cp1,cchart1,dataseries1,cEXP1);
		cb1 = new myButton(curDir+"/pic/back.png");
		ddb1 = new DataDownButton(chart1,dataseries1,1,cmb1,dub1);
		dub1 = new DataUpButton(chart1,dataseries1,1,cmb1,ddb1);
		gtmc1 = new GoToMeasuringChart(curDir+"/pic/back.png",cmb1,dataseries1,1);
		cbase1_1 = new BasePlace();
		spacer1 = new BasePlace();

		DisplayInterval di = new DisplayInterval();
		DisplayMeasure dm = new DisplayMeasure();
		DisplayCharacters dmin = new DisplayCharacters("min");
		DisplayCharacters dfilename = new DisplayCharacters("    Filename");
		DisplayCharacters dsec = new DisplayCharacters("sec");
		DisplayCharacters dci = new DisplayCharacters("min_CCD_Interval");
		JFreeChart[] chartArrays = {chart1,chart2, chart3,chart4,chart5};
		MyGraphRescaleButton mrb = new MyGraphRescaleButton(curDir+"/pic/back.png",chartArrays);
		
		int row=2;
		constraints.fill = GridBagConstraints.BOTH;	
		setConstraints(panel,gridbag,di,0,0,0,row-2,1,1);
		setConstraints(panel,gridbag,sittb,0,0,1,row-2,1,1,10,0);
		setConstraints(panel,gridbag,dmin,0,0,2,row-2,1,1);
		setConstraints(panel,gridbag,dfilename,0,0,3,row-2,1,1);	
		setConstraints(panel,gridbag,ftb,0,0,4,row-2,10,1);
		
		
		
		setConstraints(panel,gridbag,dm,0,0,0,row-1,1,1);
		setConstraints(panel,gridbag,smttb,0,0,1,row-1,1,1,10,0);
		setConstraints(panel,gridbag,dsec,0,0,2,row-1,1,1);
		setConstraints(panel,gridbag,mrb,0,0,3,row-1,1,1);
		setConstraints(panel,gridbag,arb,0,0,4,row-1,1,1); //Arduinoのボタン
		setConstraints(panel,gridbag,ictb,0,0,5,row-1,1,1); //IntervalCCDTextBox
		setConstraints(panel,gridbag,dci,0,0,6,row-1,1,1); 
		
		setConstraints(panel,gridbag,cEXP1,0,0,0,row+0,1,1);
		setConstraints(panel,gridbag,cbase1,10,100,GridBagConstraints.RELATIVE,row+0,1,7);
		setConstraints(panel,gridbag,cchart1,100,50,GridBagConstraints.RELATIVE,row+0,4,7);
		setConstraints(panel,gridbag,cmb1,0,0,0,row+1,1,1);
		setConstraints(panel,gridbag,csb1,0,0,0,row+2,1,1);
		setConstraints(panel,gridbag,gtmc1,0,0,0,row+3,1,1);
		setConstraints(panel,gridbag,dub1,0,0,0,row+4,1,1);
		setConstraints(panel,gridbag,ddb1,0,0,0,row+5,1,1);
		setConstraints(panel,gridbag,cbase1_1,0,0,0,row+6,1,1);
		setConstraints(panel,gridbag,spacer1,10,50,0,row+7,GridBagConstraints.REMAINDER,1);
		row=row+8;
		
		
		
		//Fifth line
		ldt = new DisplayLDTime();
		cbase5_0= new BasePlace();
		ctb = new CurrentTextBox();
		dlttb = new DelayLDTimeTextBox();
		cbase5 = new BasePlace();
		ldcctb = new LDCycleCounterTextBox();
		lonb = new MyLightButton(curDir+"/pic/starfull.png",ctb, ck, cs);
		loffb = new MyLightOFFButton(curDir+"/pic/starzero.png",ck, cs);
		ldb = new MyLDButton(this, ck, cchart5, dataseries5, ctb, ldt, dlttb, cs, ldcctb);
		ddddldb = new MyDDDDDLButton(this, ck, cchart5, dataseries5, ctb,ldt, dlttb, cs);
		lnb = new MyLightNoiseButton(this, ck, cchart5, dataseries5, ctb, dlttb,cs);
		cbase5_1 = new BasePlace();
		spacer5 = new BasePlace();
		
		
//		constraints.fill = GridBagConstraints.BOTH;	
		setConstraints(panel,gridbag,cbase5_0,0,0,0,row+0,1,1);
		setConstraints(panel,gridbag,cbase5,10,100,GridBagConstraints.RELATIVE,row+0,1,9);
		setConstraints(panel,gridbag,cchart5,100,50,GridBagConstraints.RELATIVE,row+0,4,9);
		setConstraints(panel,gridbag,ctb,0,0,0,row+1,1,1);
		setConstraints(panel,gridbag,lonb,0,0,0,row+2,1,1);
		setConstraints(panel,gridbag,loffb,0,0,0,row+3,1,1);
		setConstraints(panel,gridbag,ldb,0,0,0,row+4,1,1);
		setConstraints(panel,gridbag,ddddldb,0,0,0,row+5,1,1);
		setConstraints(panel,gridbag,lnb,0,0,0,row+6,1,1);
		setConstraints(panel,gridbag,cbase5_1,0,0,0,row+7,1,1);
		setConstraints(panel,gridbag,ldt,0,0,0,row+8,1,1);
		setConstraints(panel,gridbag,dlttb,0,0,0,row+9,1,1); //LDサイクルをどれだけ遅らせてスタートするか
		setConstraints(panel,gridbag,ldcctb,0,0,0,row+10,1,1); //LDサイクルをかける日数
		setConstraints(panel,gridbag,spacer5,10,50,0,row+11,GridBagConstraints.REMAINDER,1);
		
		makeFlame(panel);

		//portcheckall(); //未実装
		
	}
	
	
//	private void portcheckall(){
//		new PortChecker(cp1,cbase1).start();
//		new PortChecker(ct,cbase4).start();
//		
////		if(connection4)setOKsignal(cbase4);
//	}
	
	class PortChecker extends Thread{
		Communicator c;
		BasePlace bp;
		public PortChecker(Communicator c, BasePlace bp){
			this.c =c;
			this.bp = bp;
		}
		
		public void run(){
			boolean ans = c.checkConnection();
			if(ans)setOKsignal(bp);
		}
		
	}
	private void setOKsignal(BasePlace x){
		x.setIcon(new ImageIcon(curDir+"/pic/cal.png"));
	}
	private void setConstraints(JPanel p, GridBagLayout gl, JComponent c, int weightx, int weighty, int gridx, int gridy, int gridwidth, int gridheight){
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;	
		gc.weightx = weightx;
		gc.weighty = weighty;
		gc.gridx = gridx;
		gc.gridy = gridy;
		gc.gridwidth = gridwidth;
		gc.gridheight = gridheight;
		gl.setConstraints(c, gc);
		p.add(c);
	}
	
	private void setConstraints(JPanel p, GridBagLayout gl, JComponent c, int weightx, int weighty, int gridx, int gridy, int gridwidth, int gridheight,int merginx,int merginy){
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.BOTH;	
		gc.weightx = weightx;
		gc.weighty = weighty;
		gc.gridx = gridx;
		gc.gridy = gridy;
		gc.gridwidth = gridwidth;
		gc.gridheight = gridheight;
		gc.insets.bottom = merginy;
		gc.insets.left= merginx;
		gl.setConstraints(c, gc);
		p.add(c);
	}
	

//	public int readEXPNO2(int portNo){
//		int ans=0;
//		String path = curDir+"/result/"+portNo;
//		File dir = new File(path);
//		String[] files = dir.list();
//		if(files.length==0)ans=1;
//		else{
//			String last = files[files.length-1];
//			ans = Integer.parseInt(last.substring(3, 7))+1;
//		}
//		
//		return ans;
//	}
	
	private void refreshExpNo(){
		cEXP1.refresh();
	}
	
//	private void filecopy(String srcPath, String destPath) 
//    throws IOException {
//    
//    FileChannel srcChannel = new
//        FileInputStream(srcPath).getChannel();
//    FileChannel destChannel = new
//        FileOutputStream(destPath).getChannel();
//    try {
//        srcChannel.transferTo(0, srcChannel.size(), destChannel);
//    } finally {
//        srcChannel.close();
//        destChannel.close();
//    }
//
//}
	
//	public void updatePanel(String filename,ChartPanel chart){
//		GridBagConstraints gc = new GridBagConstraints();
//		gc.weightx = 100.0;
//		gc.weighty = 100.0;
//		gc.gridx = GridBagConstraints.RELATIVE;
//		gc.gridy =0;
//		gc.gridwidth= 4;
//		gc.gridheight = GridBagConstraints.REMAINDER;
//		this.cchart1 = makechart(filename);
//		gridbag.setConstraints(chart, gc);
//		panel.add(chart);	
//	}
//	
	
	
	
/*	public JFreeChart makechart(String filename, XYSeries dataSeries){
		// ステップ1:data setオブジェクトの作成
		File f = new File(filename);
		//ファイルがなかったら生成
		if(!f.exists())
			try {
				filecopy(curDir+"/input/tmpOriginal.txt",filename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		createXYDataset(filename,dataSeries);
		// ステップ2:data setからJFreeChartオブジェクトを作成、及び設定
//		JFreeChart freeChart = createChart(dataset);
		
		return freeChart;
	}*/

//	private static XYDataset createXYDataset(String filename, XYSeries s1) {
//		MatrixFileReader mfr = new MatrixFileReader(filename);
//		String[][] strdata = mfr.readStringMatrix();
//		if(strdata.length!=0){
//			double[][] data = normalizeData(strdata);	
//			for(int i=0;i<data.length;i++){
//				s1.add(data[i][0],data[i][1]);
//			}
//		}
//
//	
//		return sc;
//	}
	
//	private static double[][] normalizeData(String[][] x){
//		double[][] ans = new double[countupNormaldata(x)][x[0].length];
//		String startTime =  findStartTime(x);
//		int cnt=0;
//		for(int i=0;i<ans.length;i++){
//			if(x[i][0].length()<10) System.out.println("Datafile includes unusual data.");
//			else{
//				ans[cnt][0] = (double)((dateToSecond(x[i][0])-dateToSecond(startTime))/1000.0/60.0);
//				ans[cnt][1] = Integer.parseInt(x[i][1]);
//				cnt++;
//			}
//		}
//		return ans;
//	}
	
//	private static String findStartTime(String[][] x){
//		String ans="1000";
//		boolean unfound=true;
//		int cnt=0;
//		while(unfound && cnt<x.length){
//			if(x[cnt][0].length()<10) cnt++;
//			else unfound=false;
//		}
//		if(cnt==x.length) cnt=0;
//		ans = x[cnt][0];
//		return ans;
//	}
//	private static int countupNormaldata(String[][] x){
//		int ans=0;
//		for(int i=0;i<x.length;i++){
//			if(x[i][0].length()<10) ans++;
//		}
//		ans = x.length-ans;
//		return ans;
//	}
//	private static long dateToSecond(String date){
//		long ans;
//		int year =Integer.parseInt(date.substring(0, 4));
//		int mon = Integer.parseInt(date.substring(4, 6));
//		int day = Integer.parseInt(date.substring(6, 8));
//		int hour = Integer.parseInt(date.substring(8, 10));
//		int min = Integer.parseInt(date.substring(10, 12));
//		int sec = Integer.parseInt(date.substring(12, 14));
//		
//		ans = new GregorianCalendar(year,mon,day,hour,min,sec).getTimeInMillis();
//
//		
//		return ans;
//	}
	
	// XYDatasetからJFreeChartを作成
	//散布図の作成
	private static JFreeChart createScatterPlot(XYDataset dataset, String ylabel) {
		JFreeChart chart = ChartFactory.createScatterPlot(
				"", // タイトル
				"", // categoryAxisLabel （カテゴリ軸、横軸、X軸のラベル）
				ylabel, // valueAxisLabel（ヴァリュー軸、縦軸、Y軸のラベル）
				dataset, // data set
				PlotOrientation.VERTICAL, //方向
				true, // legend
				false, // tool tips
				false); // URLs

		//凡例を出力しない
		chart.getLegend().setVisible(false);
		// 出力される円グラフの透明度、境界線の色／有無などの設定を行う：クラスPlot（時系列グラフはCategoryPlot）
		XYPlot plot = (XYPlot) chart.getPlot();
		// 背景色 透明度
		plot.setBackgroundAlpha(0.5f);
		// 前景色 透明度
		plot.setForegroundAlpha(0.5f);
		// 横軸の最大値最小値設定
		plot.getDomainAxis().setLowerBound(0);
		plot.getDomainAxis().setUpperBound(240);
		// その他設定は XYPlotを参照
		ValueAxis vAxis = (ValueAxis) plot.getDomainAxis();
		TickUnits tickUnits = new TickUnits();
		//目盛り
		boolean memori =true;
		if(memori){
		int interval=24;
		TickUnit unit = new NumberTickUnit(interval);
		tickUnits.add(unit); 
		vAxis.setStandardTickUnits(tickUnits);
		}
		//createChartPrintJob
		//

		return chart;
	}
	
	// XYDatasetからJFreeChartを作成
	//折れ線グラフの作成
	private static JFreeChart createStepPlot(XYDataset dataset, String ylabel) {
		JFreeChart chart = ChartFactory.createXYLineChart(
				"", // タイトル
				"", // categoryAxisLabel （カテゴリ軸、横軸、X軸のラベル）
				ylabel, // valueAxisLabel（ヴァリュー軸、縦軸、Y軸のラベル）
				dataset, // data set
				PlotOrientation.VERTICAL, //方向
				true, // legend
				false, // tool tips
				false); // URLs

		//凡例を出力しない
		chart.getLegend().setVisible(false);
		// 出力される円グラフの透明度、境界線の色／有無などの設定を行う：クラスPlot（時系列グラフはCategoryPlot）
		XYPlot plot = (XYPlot) chart.getPlot();
		// 背景色 透明度
		plot.setBackgroundAlpha(0.5f);
		// 前景色 透明度
		plot.setForegroundAlpha(0.5f);
		// 横軸の最大値最小値設定
		plot.getDomainAxis().setLowerBound(0);
		plot.getDomainAxis().setUpperBound(240);
		// その他設定は XYPlotを参照
		ValueAxis vAxis = (ValueAxis) plot.getDomainAxis();
		TickUnits tickUnits = new TickUnits();
		//目盛り
		boolean memori =true;
		if(memori){
		int interval=24;
		TickUnit unit = new NumberTickUnit(interval);
		tickUnits.add(unit); 
		vAxis.setStandardTickUnits(tickUnits);
		}
		//createChartPrintJob
		//

		return chart;
	}
	
	
	public void makeFlame(JPanel panel){
		final JFrame frame =this;
		
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { 
				Object[] options = { "OK", "Cancel" };
				  int retValue = JOptionPane.showOptionDialog(frame,
				        "<html>Are you sure to exit?</html>",
				        "Exit Options",
				        JOptionPane.OK_CANCEL_OPTION,
				        JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				  if(retValue ==0) {
					  if(cp1!=null) cp1.portfastclose();
					  if(ct!=null) ct.portfastclose();
					  refreshExpNo();
					  System.exit(0);
				  }

				}
		});
		this.getContentPane().add( panel, BorderLayout.CENTER );
		this.pack();
		this.setSize(new Dimension(1000,740));
		this.setTitle("BioLuminescenceView"+version+"Machine"+this.MACHINENO);
	}
	
	private void osCheck(){
		String os = System.getProperty("os.name");
		if(os.contains("Windows")) isWindows =true;
		if(isWindows) curDir = "C:\\Users\\alskd\\eclipse-workspace\\BLV";
		else curDir = "/Applications/BLV";
	}
	
//	private void setMachineNo(){
//		MachineNoReader mnr = new MachineNoReader();
//		this.MACHINENO = mnr.getMachineNo();
//	}
	
//	private void setDisplayNo(){
//		displayNo1 = new DisplayNo(expno1);
//		displayNo2 = new DisplayNo(expno2);
//		displayNo3 = new DisplayNo(expno3);
//		displayNo4 = new DisplayNo(expno4);
//	}
	
	private void setDatasets(){
		dataset1.addSeries(dataseries1);
		dataset2.addSeries(dataseries2);
		dataset3.addSeries(dataseries3);
		dataset4.addSeries(dataseries4);
		dataset5.addSeries(dataseries5);
	}
	
	private void setChartPanel(){
		cchart1 = new ChartPanel(chart1);
		cchart5 = new ChartPanel(chart5);
		
	}


	public static void main(String[] args) {
		new BLV().setVisible(true);
	}

}
