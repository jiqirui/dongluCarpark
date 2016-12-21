package com.donglu.carpark.hardware.bx;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dongluhitec.card.util.ThreadUtil;

import onbon.bx05.Bx5GEnv;
import onbon.bx05.Bx5GScreenClient;
import onbon.bx05.area.DateStyle;
import onbon.bx05.area.DateTimeBxArea;
import onbon.bx05.area.TextCaptionBxArea;
import onbon.bx05.area.TimeStyle;
import onbon.bx05.area.WeekStyle;
import onbon.bx05.area.page.TextBxPage;
import onbon.bx05.cmd.dyn7.DynamicBxAreaRule;
import onbon.bx05.file.ProgramBxFile;
import onbon.bx05.utils.DisplayStyleFactory;
import onbon.bx05.utils.TextBinary.Alignment;

public class AnKangBXScreenServiceImpl implements BXScreenService {
	
	private static final String FONT_NAME = "宋体";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private static TextCaptionBxArea areaMainInfo;
	private static TextCaptionBxArea plateInfoFirst;
	private static TextCaptionBxArea plateInfoSencond;
	

	public static void main(String[] args) throws Exception {
		long currentTimeMillis = System.currentTimeMillis();
		AnKangBXScreenServiceImpl c=new AnKangBXScreenServiceImpl();
		c.init();
		Bx5GScreenClient screen = new Bx5GScreenClient("MyScreen");
		if (!screen.connect("172.16.1.232", 5005)) {
			System.out.println("connect failed");
			return;
		}

		ProgramBxFile program = c.createProgram1(screen);

		// 傳送
		screen.deleteAllDynamic();
		screen.writeProgram(program);
		screen.writeDynamic(c.dynRule, areaMainInfo);
		ProgramBxFile program2 = c.createProgram2(screen);
		screen.writeProgramQuickly(program2);
//		screen.writeDynamic(c.dynRulePlateInfo1, plateInfoFirst);
//		screen.writeDynamic(c.dynRulePlateInfo2, plateInfoSencond);
		System.out.println("currentTimeMillis======" + (System.currentTimeMillis() - currentTimeMillis));
		Thread.sleep(5000);
//		page1.setText(" 1 3 5 7 9 单号通行");
		System.out.println("currentTimeMillis======" + (System.currentTimeMillis() - currentTimeMillis));
		Thread.sleep(5000);
//		c.showPlateNO(screen);
		screen.disconnect();
	}
	private Map<String, Bx5GScreenClient> mapScreen=new HashMap<>();
	private Map<String, Date> mapIpToLastPlateShowDate=new HashMap<>();
	private Map<String, List<String>> mapScreenInfo=new HashMap<>();
	private Map<String, Boolean> mapPlateStatus=new HashMap<>();
	private ExecutorService fixedThreadPool;
	private Date initDate=new Date();
	
	private ProgramBxFile bxFile2;
	
	private List<String> listWaitInPlate=new ArrayList<>();
	private List<String> listLastWaitInPlate=new ArrayList<>();
	private boolean plateControlSetting;
	private boolean lastPlateControlSetting;
	private DynamicBxAreaRule dynRule;
	private DynamicBxAreaRule dynRulePlateInfo1;
	private DynamicBxAreaRule dynRulePlateInfo2;
	public void sendPositionToBxDevice(String ip,int position) throws Exception{
		Bx5GScreenClient screen = mapScreen.get(ip);
		if (screen==null) {
			screen = new Bx5GScreenClient("MyScreen");
			if (!screen.connect("172.16.1.232", 5005)) {
				System.out.println("connect failed");
				return;
			}
			mapScreen.put(ip, screen);
			createProgram1(screen);
			createProgram2(screen);
		}
		
	}

	public void showPlateNO(Bx5GScreenClient screen) throws Exception {
		screen.lockProgram("P001", 15);
		// 增加文本頁
		TextBxPage page1 = new TextBxPage(" 陕G12345  准予通行", new Font(FONT_NAME, Font.PLAIN, 14), Color.green, Color.black);
		page1.setDisplayStyle(DisplayStyleFactory.getStyle(1));
		page1.setVerticalAlignment(Alignment.CENTER); // 垂直置中
		plateInfoFirst.clearPages();
		plateInfoFirst.addPage(page1);

		// 增加文本頁
		TextBxPage page2 = new TextBxPage(" 陕G12345  禁止通行", new Font(FONT_NAME, Font.PLAIN, 14), Color.red, Color.black);
		page2.setDisplayStyle(DisplayStyleFactory.getStyle(1));
		page2.setVerticalAlignment(Alignment.CENTER); // 垂直置中

		plateInfoSencond.addPage(page2);
		screen.deleteAllDynamic();
		screen.writeDynamic(dynRulePlateInfo1, plateInfoFirst);
		screen.writeDynamic(dynRulePlateInfo2, plateInfoSencond);
	}

	public void init() throws Exception {
		Bx5GEnv.initial("log.properties", 15000);
		ScheduledExecutorService newSingleThreadScheduledExecutor = Executors.newSingleThreadScheduledExecutor(ThreadUtil.createThreadFactory("发送车位到BX屏幕"));
		newSingleThreadScheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			private int size=0;

			@Override
			public void run() {
				try {
					Set<String> keySet = mapScreen.keySet();
					if (keySet.isEmpty()) {
						return;
					}
					for (String ip : keySet) {
						Bx5GScreenClient screen = mapScreen.get(ip);
						if (mapScreen.get(ip)==null) {
							return;
						}
						Date lastPlateShowDate=mapIpToLastPlateShowDate.getOrDefault(ip,initDate);
						if (System.currentTimeMillis()-lastPlateShowDate.getTime()<15000)  {
							return;
						}
						if (listWaitInPlate.size()>0&&listLastWaitInPlate.toString().equals(listWaitInPlate.toString())&&lastPlateControlSetting==plateControlSetting) {
							if (size++<listLastWaitInPlate.size()) {
								return;
							}else{
								size=0;
							}
							
						}
						showMainInfo(screen);
						listLastWaitInPlate=listWaitInPlate;
						lastPlateControlSetting=plateControlSetting;
					}
				} catch (Exception e) {
					logger.info("发送平时屏时发生错误");
				}
			}
		}, 5, 5, TimeUnit.SECONDS);
	}

	protected void showMainInfo(Bx5GScreenClient screen) throws Exception {
		long currentTimeMillis = System.currentTimeMillis();
		screen.lockProgram("P000", 65535);
		screen.deleteAllDynamic();
		TextCaptionBxArea area = new TextCaptionBxArea(0, 32, 160, 32, screen.getProfile());
		area.setFrameShow(false);
		
		if (listWaitInPlate.isEmpty()) {
			String string = " 0 2 4 6 8  双号通行";
			if (plateControlSetting) {
				string = " 1 3 5 7 9  单号通行";
			}
			TextBxPage p = new TextBxPage(string, new Font(FONT_NAME, Font.PLAIN, 14), Color.green, Color.black);
			p.setDisplayStyle(DisplayStyleFactory.getStyle(1));
			p.setVerticalAlignment(Alignment.CENTER); // 垂直置中
			p.setHorizontalAlignment(Alignment.CENTER);
			area.addPage(p);
//			return;
		}
		for (String string : listWaitInPlate) {
			TextBxPage page1 = new TextBxPage(" "+string+" 等待通行", new Font(FONT_NAME, Font.PLAIN, 14), Color.green, Color.black);
			page1.setDisplayStyle(DisplayStyleFactory.getStyle(5));
			page1.setSpeed(4);
			page1.setStayTime(500);
			page1.setVerticalAlignment(Alignment.CENTER); // 垂直置中
			area.addPage(page1);
		}
		screen.writeDynamic(dynRule, area);
		listLastWaitInPlate=listWaitInPlate;
		System.out.println("更新主页信息花费时间===="+(System.currentTimeMillis()-currentTimeMillis));
	}
	
	/**
	 * @param screen
	 * @return
	 * @throws IOException
	 */
	public ProgramBxFile createProgram1(Bx5GScreenClient screen) throws IOException {
		DateTimeBxArea dtArea = new DateTimeBxArea(1, 0, 158, 16, screen.getProfile());
		dtArea.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
		// 设置颜色
		dtArea.setColor(Color.green);
		// 多行显示还是单行显示
		dtArea.setMultiline(false);
		//
		// 年月日的显示方式
		// 如果不显示，则设置为 null
		dtArea.setDateStyle(DateStyle.YYYY_MM_DD_3);
		dtArea.setTimeStyle(TimeStyle.HH_MM_2);
		dtArea.setWeekStyle(null);

		DateTimeBxArea dtArea1 = new DateTimeBxArea(60, 16, 39, 16, screen.getProfile());
		dtArea1.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
		// 设置颜色
		dtArea1.setColor(Color.green);
		// 多行显示还是单行显示
		dtArea1.setMultiline(false);
		//
		// 年月日的显示方式
		// 如果不显示，则设置为 null
		dtArea1.setDateStyle(null);
		dtArea1.setTimeStyle(null);
		dtArea1.setWeekStyle(WeekStyle.CHINESE);

//		// 增加文本頁
		String string = " 0 2 4 6 8  双号通行";
		if (plateControlSetting) {
			string = " 1 3 5 7 9  单号通行";
		}
		TextBxPage p = new TextBxPage(string, new Font(FONT_NAME, Font.PLAIN, 14), Color.green, Color.black);
		p.setDisplayStyle(DisplayStyleFactory.getStyle(1));
		p.setVerticalAlignment(Alignment.CENTER); // 垂直置中
		p.setHorizontalAlignment(Alignment.CENTER);
		

		areaMainInfo = new TextCaptionBxArea(0, 32, 160, 32, screen.getProfile());
		areaMainInfo.setFrameShow(false);
		
		
		TextCaptionBxArea areaMainInfo = new TextCaptionBxArea(0, 32, 160, 32, screen.getProfile());
		areaMainInfo.setFrameShow(false);
		areaMainInfo.addPage(p);

		// 增加文本頁
		TextBxPage page = new TextBxPage("安康市人民政府", new Font(FONT_NAME, Font.PLAIN, 14), Color.red, Color.black);
		page.setDisplayStyle(DisplayStyleFactory.getStyle(1));
		page.setVerticalAlignment(Alignment.CENTER); // 垂直置中

		// 增加圖文區
		TextCaptionBxArea area = new TextCaptionBxArea(24, 64, 112, 32, screen.getProfile());
		area.setFrameShow(false);
		area.setFrameStyle(2);
		area.loadFrameImage(6);
		area.addPage(page);
		// 建立節目
		ProgramBxFile program = new ProgramBxFile("P000", screen.getProfile());
		program.setProgramTimeSpan(65535);
		program.addArea(dtArea);
		program.addArea(dtArea1);
		program.addArea(areaMainInfo);
		program.addArea(area);
		dynRule = new DynamicBxAreaRule(0, (byte)0, (byte)0, 0);
		dynRule.addProgram("P000");
		return program;
	}
	
	/**
	 * @param screen
	 * @return
	 * @throws IOException
	 */
	public ProgramBxFile createProgram2(Bx5GScreenClient screen) throws IOException {
		
		// 增加文本頁
		TextBxPage page = new TextBxPage("车辆实时状态", new Font(FONT_NAME, Font.PLAIN, 12), Color.red, Color.black);
		page.setDisplayStyle(DisplayStyleFactory.getStyle(1));
		page.setVerticalAlignment(Alignment.CENTER); // 垂直置中

		// 增加圖文區
		TextCaptionBxArea area = new TextCaptionBxArea(30, 0, 100, 16, screen.getProfile());
		area.setFrameShow(false);
		area.setFrameStyle(2);
		area.loadFrameImage(6);
		area.addPage(page);
		String string = " 0 2 4 6 8  双号通行";
		if (plateControlSetting) {
			string = " 1 3 5 7 9  双号通行";
		}
		// 增加文本頁
		TextBxPage page3 = new TextBxPage("安康市人民政府", new Font(FONT_NAME, Font.PLAIN, 14), Color.red, Color.black);
		page3.setDisplayStyle(DisplayStyleFactory.getStyle(1));
		page3.setVerticalAlignment(Alignment.CENTER); // 垂直置中

		// 增加圖文區
		TextCaptionBxArea area3 = new TextCaptionBxArea(0, 64, 160, 32, screen.getProfile());
		area3.setFrameShow(false);
		area3.setFrameStyle(2);
		area3.loadFrameImage(6);
		area3.addPage(page3);
		
		dynRulePlateInfo1 = new DynamicBxAreaRule(1, (byte)0, (byte)0, 0);
		dynRulePlateInfo2 = new DynamicBxAreaRule(2, (byte)0, (byte)0, 0);
		
		plateInfoFirst = new TextCaptionBxArea(0, 16, 160, 24, screen.getProfile());
		plateInfoFirst.setFrameShow(false);
		plateInfoFirst.setFrameStyle(2);
		plateInfoFirst.loadFrameImage(6);
		
		plateInfoSencond = new TextCaptionBxArea(0, 40, 160, 24, screen.getProfile());
		plateInfoSencond.setFrameShow(false);
		plateInfoSencond.setFrameStyle(2);
		plateInfoSencond.loadFrameImage(6);
		
		// 建立節目
		ProgramBxFile program = new ProgramBxFile("P001", screen.getProfile());
		program.setProgramTimeSpan(10);
//		program.addArea(plateInfoFirst);
//		program.addArea(plateInfoSencond);
		program.addArea(area3);
		program.addArea(area);
		dynRulePlateInfo1.addProgram("P001");
		dynRulePlateInfo2.addProgram("P001");
		return program;
	}
	@Override
	public boolean sendContent(int identitifire, String ip, String content) {
		
		return false;
	}
	@Override
	public boolean sendPosition(int identitifire, String ip, int position) {
		try {
			initScreen(ip);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	private Bx5GScreenClient initScreen(String ip) throws Exception{
		Bx5GScreenClient screen = mapScreen.get(ip);
		if (screen == null) {
			logger.info("初始化屏幕：{}",ip);
			screen = new Bx5GScreenClient("MyScreen");
			if (!screen.connect(ip, 5005)) {
				System.out.println("connect failed");
				return null;
			}
			mapScreen.put(ip, screen);
			screen.deletePrograms();
			screen.deleteAllDynamic();
			ProgramBxFile bxFile = createProgram1(screen);
			bxFile2 = createProgram2(screen);
			screen.writeProgram(bxFile);
			screen.writeProgram(bxFile2);
		}
		return screen;
	}
	@Override
	public boolean sendPlateNO(int identitifire, String ip, String plateNO, boolean isTrue) {
		try {
			mapIpToLastPlateShowDate.put(ip, new Date());
			long currentTimeMillis = System.currentTimeMillis();
			final Bx5GScreenClient screen = initScreen(ip);
			if (screen == null) {
				return false;
			}
			mapScreen.put(ip, screen);
			
			final List<String> list = mapScreenInfo.getOrDefault(ip, new ArrayList<>());
			if (!list.contains(plateNO)) {
				list.add(plateNO);
			}
			if (list.size()>2) {
				String remove = list.remove(0);
				mapPlateStatus.remove(remove);
			}
			mapScreenInfo.put(ip, list);
			mapPlateStatus.put(plateNO, isTrue);
			
			if (fixedThreadPool==null) {
				fixedThreadPool = Executors.newFixedThreadPool(1);
			}
			System.out.println(plateNO+"==="+list);
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						screen.deleteAllDynamic();
						screen.lockProgram("P001", 25);
						String firstPlate = list.size() > 1 ? list.get(1) : list.get(0);
						String s = "准予通行";
						Color color = Color.green;
						if (!mapPlateStatus.get(firstPlate)) {
							s = "禁止通行";
							color=Color.red;
						}
						// 增加文本頁
						TextBxPage page1 = new TextBxPage(" " + firstPlate + "  " + s, new Font(FONT_NAME, Font.PLAIN, 12), color, Color.black);
						page1.setDisplayStyle(DisplayStyleFactory.getStyle(1));
						page1.setVerticalAlignment(Alignment.CENTER); // 垂直置中
						plateInfoFirst.clearPages();
						plateInfoFirst.addPage(page1);
						screen.writeDynamic(dynRulePlateInfo1, plateInfoFirst);
						firstPlate = list.size() > 1 ? list.get(0) : null;
						if (firstPlate != null) {
							s = "准予通行";
							color = Color.green;
							if (!mapPlateStatus.get(firstPlate)) {
								s = "禁止通行";
								color=Color.red;
							}
							// 增加文本頁
							TextBxPage page2 = new TextBxPage(" "+firstPlate+"  "+s, new Font(FONT_NAME, Font.PLAIN, 12), color, Color.black);
							page2.setDisplayStyle(DisplayStyleFactory.getStyle(1));
							page2.setVerticalAlignment(Alignment.CENTER); // 垂直置中
							plateInfoSencond.clearPages();
							plateInfoSencond.addPage(page2);
							screen.writeDynamic(dynRulePlateInfo2, plateInfoSencond);
						}
						long l=System.currentTimeMillis()-currentTimeMillis;
						System.out.println("发送车牌到显示屏花费时间："+l);
						mapIpToLastPlateShowDate.put(ip, new Date());
					} catch (Exception e) {
						logger.error("发送车牌：{} 到：{} 时发生错误",ip,plateNO);
					}
				}
			};
			fixedThreadPool.submit(runnable);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return false;
	}
	@Override
	public boolean init(int handle) {
		try {
			init();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public void setPlateControlStatus(boolean plateControlSetting) {
		this.plateControlSetting = plateControlSetting;
	}
	@Override
	public void setWillInPlate(List<String> willInPlate) {
		listWaitInPlate=willInPlate;
	}
	
}