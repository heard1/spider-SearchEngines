package homework4;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
	public static void main1(String[] args) throws InterruptedException {//爬虫主循环，爬取30000个网页
		for(int i=500; i<=30000; i++) {//i用于确认网页url
			if (banError==10) {
				System.out.println("begin to wait");
				Thread.sleep(1000*60*10);//等解ban
				banError=0;
			}
			Document html = getHTML("https://dxy.com/disease/"+i);//html内容
			if(html != null) analyzeHTML(html);//html内容提取保存到本地
			Thread.sleep(3000);//防止ip被ban得太快
		}
	}
	
	static int numOfDisease=0;//用于记录问答的个数
	static int banError=0;//ip被ban记录
	//根据url获取html内容
	static Document getHTML(String url){
		Document html = null;
		try {
			html = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
					.timeout(1000)
					.get();
			//System.out.println(html);
		}
		catch (IOException e) {
			//e.printStackTrace();
			System.out.println(url+" has a error!");//遇到404问题
			banError++;
		}
		return html;
	}
	
	static void analyzeHTML(Document html) {
		if(html.title().equals("undefined症状_病因_治疗方法_鉴别_专家咨询|丁香医生")) {//如果是个空网页或被ban
			System.out.println("pass");
			return;
		}
		else {
			banError=0;
			System.out.println("analyze:"+numOfDisease);//控制台输出个数
			numOfDisease++;
			File file=new File("./info/"+numOfDisease);
			file.mkdir();
			Elements elements = html.select(".disease-card-info-title");//页面用选择器选择class=“”的
			//System.out.println(elements.text());
			try {
				FileWriter writer=new FileWriter("./info/"+numOfDisease+"/0.txt");
				writer.write(elements.text());
				writer.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}

			elements = html.select(".disease-card-info-content");//页面用选择器选择class=“”的
			//System.out.println(elements.text());
			try {
				FileWriter writer=new FileWriter("./info/"+numOfDisease+"/1.txt");
				writer.write(elements.text());
				writer.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}
			
			Iterator<Element> Ele=html.select(".disease-detail-card-deatil").iterator();//存在多个符合条件项，用迭代器
			int i=2;//用于存储txt，命名从1到7
			while (Ele.hasNext()) {
				Element next = Ele.next();
				try {
					FileWriter writer=new FileWriter("./info/"+numOfDisease+"/"+i+".txt");
					writer.write(next.text());
					writer.close();
					i++;
				}
				catch (IOException e){
					e.printStackTrace();
				}
			}
			if(i != 8) numOfDisease--;//如果不是8 可能录入的不是疾病，（药什么的）删除
		}
	}
}
