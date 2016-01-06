package utils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import play.Logger;



/**
 * @author luo
 *
 */
public class EmailService extends Thread{
	private static final Logger.ALogger LOGGER =Logger.of(EmailService.class);
	private static EmailService instance = new EmailService();
    private static final  String SENDCLOUD_SMTP_HOST = "smtp.qiye.163.com";
    private static final  int SENDCLOUD_SMTP_PORT = 25;
    private static Session mailSession;
    private static Transport transport;
	private Executor executor = Executors.newSingleThreadExecutor();
	private static final String contentModifyKjtPrice = "<p>［嗨个购］跨境通提示：存在商品价格发生改变，请核实并做相应处理！\t   %s</p>";
	private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

	static{
		try {
			// 配置javamail
			Properties props = System.getProperties();
			props.setProperty("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", SENDCLOUD_SMTP_HOST);
			props.setProperty("mail.smtp.auth", "true");
			props.put("mail.smtp.connectiontimeout", 10000); // 单位：毫秒
			props.put("mail.smtp.timeout", 10000); // 单位：毫秒

			props.setProperty("mail.mime.encodefilename", "true");

			// 使用api_user和api_key进行验证
			final String apiUser = "lvcheng@neolix.cn";
			final String apiKey = "nicai123";

			mailSession = Session.getInstance(props, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(apiUser, apiKey);
				}
			});
		} catch (Exception e) {
			LOGGER.error("init email service error.", e);
		}
	}


	 /* 私有构造方法，防止被实例化 */  
	private EmailService(){this.start();}
	//获取配置文件，初始化sender；
	public static EmailService getInstance(){
		return instance;
	}

	public void run(){
		LOGGER.info("start sms service ");
		Runnable r;
		try {
			while((r = tasks.take()) != null){
				executor.execute(r);
			}
		} catch (InterruptedException e) {
			LOGGER.error("InterruptedException in sms service",e);
		}
	}
	
	/**
	 * 简单下发
	 * @param to
	 * @param content
	 * @return
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private void sendEmail(String to,String subject,String content) {
		SendEmailThread sendEmailThread = new SendEmailThread(to, subject, content);
		try {
			tasks.put(sendEmailThread);
		} catch (InterruptedException e) {
			LOGGER.error("send email service add task error.", e);
		}
	}
	
	
	
	/**
	 * 简单下发可以指定多个用户
	 * @param toList
	 * @param subject
	 * @param content
	 */
	@SuppressWarnings("unused")
	private void sendEmailList(List<String> toList,String subject,String content) {
		SendMessageThread message = new SendMessageThread(toList,subject,content);
		(new Thread(message)).start();
	}
	
	/**
	 * 异步发送邮件内部类
	 *
	 */
	class SendMessageThread extends Thread {
		List<String> toList;
		String subject;
		String content;

		public SendMessageThread(List<String> toList, String subject,String content) {
			this.toList = toList;
			this.subject = subject;
			this.content = content;
		}

		String smsmsg = "";
		public void run() {
			synchronized (smsmsg) {
				for (String to: toList) {
					sendEmail( to, subject, content);
				}
			}
		}
	}
	
	
	/**
	 * 邀请用户进行激活的提醒
	 * @param toUser
	 * @param url 激活的URL
	 */
	public void sendEmailWithKjtProductPrice(String toUser,String content){
		String subject = "跨境通商品价格变动";
		content = String.format(contentModifyKjtPrice, content);
		sendEmail( toUser, subject, content);
	}
	
	/**
	 * 异步发送邮件内部类
	 *
	 */
	class SendEmailThread extends Thread {
		String to;
		String subject;
		String content;

		public SendEmailThread(String to, String subject,String content) {
			this.to = to;
			this.subject = subject;
			this.content = content;
		}

		String smsmsg = "";
		public void run() {
			synchronized (smsmsg) {
				try {
					 // 添加html形式的邮件正文
			        BodyPart part1 = new MimeBodyPart();
			        part1.setHeader("Content-Type", "text/html;charset=UTF-8");
			        part1.setHeader("Content-Transfer-Encoding", "base64");
			        part1.setContent(content, "text/html;charset=UTF-8");
			        Multipart multipart = new MimeMultipart("alternative");
			        multipart.addBodyPart(part1);
			        
			        MimeMessage message = new MimeMessage(mailSession);
			        message.setContent(multipart);

			        // 发信人，用正确邮件地址替代 
			        message.setFrom(new InternetAddress("lvcheng@neolix.cn", "吕诚", "UTF-8"));
			        // 收件人地址，用正确邮件地址替代
			        message.addRecipient(RecipientType.TO, new InternetAddress(to));
			        // 邮件主题
			        message.setSubject(subject, "UTF-8");
			        LOGGER.info("send email to " + to + " subject " + subject);
			        // 连接sendcloud服务器，发送邮件
			        transport = mailSession.getTransport();
			        transport.connect();
			        transport.sendMessage(message, message.getRecipients(RecipientType.TO));
			        transport.close();
				} catch (Exception e) {
					LOGGER.error("send email to " + to + " subject " + subject,e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		EmailService.getInstance().sendEmailWithKjtProductPrice("chentaotao@higegou.com","nihao ,,,cccsldfjlsdjflsdf");
	}
}
