package homework4;

import java.io.*;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Search {
	public static void main(String[] args) {
		Search w=new Search();
		String filePath="./index";//创建索引的存储目录
		
		//w.createIndex(filePath);//创建索引
		//System.out.println("finish create index");
		while(true) {
			//得到查询方向、内容、显示条数的信息
			String direct = null;
			int NumOfPrint = 20;
			Scanner sc = new Scanner(System.in);
			System.out.println("请选择查询方向（0--全体、1--名称、2--细节、3--症状、4--病因）：");
			try{
				int direction=sc.nextInt();
				if(direction==1) direct="name";
				else if(direction==2) direct="detail";
				else if(direction==3) direct="symptom";
				else if(direction==4) direct="reason";
				else if(direction!=0) System.out.println("非指定数字，默认全体搜索");
			}
			catch(Exception e) {
				System.out.println("非指定数字，默认全体搜索");
			}
			System.out.println("请指定输出条数（>0)：");
			try{
				NumOfPrint=sc.nextInt();
				if(NumOfPrint<=0) {
					NumOfPrint=20;
					System.out.println("不得小于等于0，默认20条");
				}
			}
			catch(Exception e) {
				System.out.println("非int型数字，默认20条");
			}
			System.out.println("请输入查询内容：");
			sc.nextLine();//消除回车
			String queryStr=sc.nextLine();
			System.out.print('\n');
			if(direct!=null)
				w.search(filePath, direct, queryStr,NumOfPrint);//搜索
			else {
				NumOfPrint/=4;//条数平均分给四个field
				if(NumOfPrint==0) NumOfPrint=1;
				w.search(filePath, "name", queryStr, NumOfPrint);//搜索
				w.search(filePath, "detail", queryStr, NumOfPrint);//搜索
				w.search(filePath, "symptom", queryStr, NumOfPrint);//搜索
				w.search(filePath, "reason", queryStr, NumOfPrint);//搜索
			}
			System.out.println("退出查询请按q，否则请按任意键");
			String judge = sc.nextLine();
			//System.out.println(judge);
			if(judge.equals("q")) {
				System.out.println("byebye");
				break;//跳出循环，结束程序
			}
		}
	}
	
	int TotalInfo = 534;//数量由爬下网页数量决定
	
	public void createIndex(String filePath){
		File f=new File(filePath);
		IndexWriter iwr=null;
		try {
			Directory dir=FSDirectory.open(f);
			Analyzer analyzer = new IKAnalyzer();
			
			IndexWriterConfig conf=new IndexWriterConfig(Version.LUCENE_4_10_0,analyzer);
			iwr=new IndexWriter(dir,conf);//建立IndexWriter。固定套路
			for(int i=1; i<=TotalInfo; i++) {
				Document doc=getDocument(i);
				iwr.addDocument(doc);//添加doc，Lucene的检索是以document为基本单位
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			iwr.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	Document getDocument(int QuestionNum){
		//doc中内容由field构成，在检索过程中，Lucene会按照指定的Field依次搜索每个document的该项field是否符合要求。
		Document doc=new Document();
		for(int i=0; i<8; i++) {
			File file=new File("./info/"+QuestionNum+"/"+i+".txt");
			Field f;
			if(i==0)      f=new TextField("name",readToString(file),Field.Store.YES);
			else if(i==1) f=new TextField("detail",readToString(file),Field.Store.YES);
			else if(i==2) f=new TextField("symptom",readToString(file),Field.Store.YES);
			else if(i==3) f=new TextField("reason",readToString(file),Field.Store.YES);
			else if(i==4) f=new TextField("diagnosis",readToString(file),Field.Store.YES);
			else if(i==5) f=new TextField("treatment",readToString(file),Field.Store.YES);
			else if(i==6) f=new TextField("life",readToString(file),Field.Store.YES);
			else  		  f=new TextField("prevention",readToString(file),Field.Store.YES);
			doc.add(f);
		}
		return doc;
	}
	
	//用于打开爬下来的txt文件并全部读取
    public static String readToString(File file) {
        Long filelength = file.length();     //获取文件长度
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(filecontent);//返回文件内容,默认编码
    }
	
	public void search(String filePath, String direct, String queryStr, int NumOfPrint){
		File f=new File(filePath);
		try {
			IndexSearcher searcher=new IndexSearcher(DirectoryReader.open(FSDirectory.open(f)));
			Analyzer analyzer = new IKAnalyzer();
			QueryParser parser = new QueryParser(Version.LUCENE_4_10_0, direct, analyzer);
			Query query=parser.parse(queryStr);
			TopDocs hits=searcher.search(query,NumOfPrint);//前面几行代码也是固定套路，使用时直接改field和关键词即可
			//int i=1;
			for(ScoreDoc doc:hits.scoreDocs){
				Document d=searcher.doc(doc.doc);
				//System.out.println(i++);
				System.out.println("名称："+d.get("name")+'\n');
				System.out.println("细节："+d.get("detail")+'\n');
				System.out.println("症状："+d.get("symptom")+'\n');
				System.out.println("病因："+d.get("reason")+'\n');
				System.out.println("诊断："+d.get("diagnosis")+'\n');
				System.out.println("治疗："+d.get("treatment")+'\n');
				System.out.println("生活："+d.get("life")+'\n');
				if(d.get("prevention").equals(null))
					System.out.println("预防："+d.get("prevention")+'\n');
				System.out.println("----------------------------------------------");
			}
		} 
		catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("查询有误，请重新输入");
		}
	}
}
