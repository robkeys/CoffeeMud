package com.planet_ink.coffee_mud.web;
import java.io.*;
import java.net.*;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.interfaces.*;

public class SMTPserver extends Thread implements Tickable
{
	public String ID(){return "SMTPserver";}
	public String name(){return "SMTPserver";}
	public long tickStatus=STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	public long lastAllProcessing=System.currentTimeMillis();
	
	public INI page=null;

	public static final float HOST_VERSION_MAJOR=(float)1.0;
	public static final float HOST_VERSION_MINOR=(float)0.0;
	public static Hashtable webMacros=null;
	public static INI iniPage=null;
	public ServerSocket servsock=null;
	public boolean isOK = false;
	private MudHost mud;
	private static boolean displayedBlurb=false;
	private static String domain="coffeemud";
	private static String mailbox=null;
	private static DVector journals=null;
											 
	public final static String ServerVersionString = "CoffeeMud SMTPserver/" + HOST_VERSION_MAJOR + "." + HOST_VERSION_MINOR;

	public SMTPserver(MudHost a_mud)
	{
		super("SMTP");
		mud = a_mud;

		if (!initServer())
			isOK = false;
		else
			isOK = true;
	}

	public MudHost getMUD()	{return mud;}
	public String domainName(){return domain;}
	public String mailboxName(){return mailbox;}

	public Properties getCommonPropPage()
	{
		if (iniPage==null || !iniPage.loaded)
		{
			iniPage=new INI("web" + File.separatorChar + "common.ini");
			if(!iniPage.loaded)
				Log.errOut("SMTPserver","Unable to load common.ini!");
		}
		return iniPage;
	}

	private boolean initServer()
	{
		if (!loadPropPage())
		{
			Log.errOut(getName(),"ERROR: SMTPserver unable to read ini file.");
			return false;
		}

		if (page.getStr("DOMAIN").trim().length()==0)
		{
			Log.errOut(getName(),"ERROR: required parameter missing: DOMAIN");
			return false;
		}
		domain=page.getStr("DOMAIN");
		mailbox=page.getStr("MAILBOX");
		if(mailbox==null) mailbox="";
		else mailbox=mailbox.trim();
		
		String journalStr=page.getStr("JOURNALS");
		if((journalStr==null)||(journalStr.length()>0))
		{
			Vector V=Util.parseCommas(journalStr,true);
			if(V.size()>0)
			{
				journals=new DVector(4);
				for(int v=0;v<V.size();v++)
				{
					String s=((String)V.elementAt(v)).trim();
					String parm="";
					int x=s.indexOf("(");
					if((x>0)&&(s.endsWith(")")))
					{
						parm=parm.substring(x+1,s.length()-1).trim();
						s=s.substring(0,x).trim();
					}
					if(!journals.contains(s))
					{
						boolean forward=(parm.toUpperCase().startsWith("FORWARD ")||parm.toUpperCase().endsWith(" FORWARD")||(parm.toUpperCase().indexOf(" FORWARD ")>=0));
						boolean subscribeOnly=(parm.toUpperCase().startsWith("SUBSCRIBEONLY ")||parm.toUpperCase().endsWith(" SUBSCRIBEONLY")||(parm.toUpperCase().indexOf(" SUBSCRIBEONLY ")>=0));
						journals.addElement(s,new Boolean(forward),new Boolean(subscribeOnly),parm);
					}
				}
			}
		}

		if (!displayedBlurb)
		{
			displayedBlurb = true;
			//Log.sysOut(getName(),"SMTPserver (C)2004 Bo Zimmerman");
		}
		if(mailbox.length()==0)
			Log.sysOut(getName(),"Player mail box system is disabled.");

		return true;
	}
	
	public String getAnEmailJournal(String journal)
	{
		if(journals==null) return null;
		journal=Util.replaceAll(journal,"_"," ");
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return (String)journals.elementAt(i,1);
		}
		return null;
	}
	public boolean isAForwardingJournal(String journal)
	{
		if(journals==null) return false;
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return ((Boolean)journals.elementAt(i,2)).booleanValue();
		}
		return false;
	}
	public boolean isASubscribeOnlyJournal(String journal)
	{
		if(journals==null) return false;
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return ((Boolean)journals.elementAt(i,3)).booleanValue();
		}
		return false;
	}
	public String getJournalCriteria(String journal)
	{
		if(journals==null) return "";
		for(int i=0;i<journals.size();i++)
		{
			if(journal.equalsIgnoreCase((String)journals.elementAt(i,1)))
				return (String)journals.elementAt(i,4);;
		}
		return "";
	}

	private boolean loadPropPage()
	{
		if (page==null || !page.loaded)
		{
			String fn = "web" + File.separatorChar + "email.ini";
			page=new INI(getCommonPropPage(), fn);
			if(!page.loaded)
			{
				Log.errOut(getName(),"failed to load " + fn);
				return false;
			}
		}
		return true;
	}

	public void run()
	{
		int q_len = 6;
		Socket sock=null;
		boolean serverOK = false;

		if (!isOK)	return;
		if ((page == null) || (!page.loaded))
		{
			Log.errOut(getName(),"ERROR: SMTPserver will not run with no properties. Shutting down.");
			isOK = false;
			return;
		}


		if (page.getInt("BACKLOG") > 0)
			q_len = page.getInt("BACKLOG");

		InetAddress bindAddr = null;


		if (page.getStr("BIND") != null && page.getStr("BIND").length() > 0)
		{
			try
			{
				bindAddr = InetAddress.getByName(page.getStr("BIND"));
			}
			catch (UnknownHostException e)
			{
				Log.errOut(getName(),"ERROR: Could not bind to address " + page.getStr("BIND"));
				bindAddr = null;
			}
		}

		try
		{
			servsock=new ServerSocket(SMTPclient.DEFAULT_PORT, q_len, bindAddr);

			Log.sysOut(getName(),"Started on port: "+SMTPclient.DEFAULT_PORT);
			if (bindAddr != null)
				Log.sysOut(getName(),"Bound to: "+bindAddr.toString());


			serverOK = true;

			while(true)
			{
				sock=servsock.accept();
				if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
				{
					ProcessSMTPrequest W=new ProcessSMTPrequest(sock,this,page);
					W.equals(W); // this prevents an initialized by never used error
					// nb - ProcessSMTPrequest is a Thread, but it .start()s in the constructor
					//  if succeeds - no need to .start() it here
				}
				else
				{
					sock.getOutputStream().write(("421 Mud down.. try later.\r\n").getBytes());
					sock.getOutputStream().flush();
					sock.close();
				}
				sock=null;
			}
		}
		catch(Throwable t)
		{
			// jef: if we've been interrupted, servsock will be null
			//   and serverOK will be true
			if((t!=null)&&(t instanceof Exception))
				Log.errOut(getName(),((Exception)t).getMessage());


			// jef: this prevents initHost() from running if run() has failed (eg socket in use)
			if (!serverOK)
				isOK = false;
		}

		try
		{
			if(servsock!=null)
				servsock.close();
			if(sock!=null)
				sock.close();
		}
		catch(IOException e)
		{
		}

		Log.sysOut(getName(),"Thread stopped!");
	}


	// sends shutdown message to both log and optional session
	// then just calls interrupt

	public void shutdown(Session S)
	{
		Log.sysOut(getName(),"Shutting down.");
		if (S != null)
			S.println( getName() + " shutting down.");
		if(getTickStatus()==Tickable.STATUS_NOT)
			tick(this,MudHost.TICK_READYTOSTOP);
		else
		while(getTickStatus()!=Tickable.STATUS_NOT)
		{try{Thread.sleep(100);}catch(Exception e){}}
		this.interrupt();
	}

	public void shutdown()	{shutdown(null);}
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickStatus!=STATUS_NOT) return true;
		
		tickStatus=STATUS_START;
		if((tickID==MudHost.TICK_READYTOSTOP)||(tickID==MudHost.TICK_EMAIL))
		{
			Hashtable rememberedAddresses=new Hashtable();
				
			// this is where it should attempt any mail forwarding
			// remember, a 5 day old private mail message is a goner
			// remember that new to all messages need to be parsed
			// for subscribe/unsubscribe and deleted, or then 
			// forwarded to all members private boxes.  Lots of work to do!
			if(journals!=null)
			for(int j=0;j<journals.size();j++)
			{
				String name=(String)journals.elementAt(j,1);
				if(isAForwardingJournal(name))
				{
					// Vector mailingList=?
					Vector msgs=CMClass.DBEngine().DBReadJournal(name);
					for(int m=0;m<msgs.size();m++)
					{
						Vector msg=(Vector)msgs.elementAt(m);
						String to=(String)msg.elementAt(DatabaseEngine.JOURNAL_TO);
						if(to.equalsIgnoreCase("ALL"))
						{
							String key=(String)msg.elementAt(DatabaseEngine.JOURNAL_KEY);
							String subj=((String)msg.elementAt(DatabaseEngine.JOURNAL_SUBJ)).trim();
							String s=((String)msg.elementAt(DatabaseEngine.JOURNAL_MSG)).trim();
							long date=Util.s_long((String)msg.elementAt(DatabaseEngine.JOURNAL_DATE));
							if((subj.equalsIgnoreCase("subscribe"))
							||(s.equalsIgnoreCase("subscribe")))
							{
								// add to mailing list
								CMClass.DBEngine().DBDeleteJournal(key);
							}
							else
							if((subj.equalsIgnoreCase("unsubscribe"))
							||(s.equalsIgnoreCase("unsubscribe")))
							{
								// remove from mailing list
								CMClass.DBEngine().DBDeleteJournal(key);
							}
							else
							{
								// split from mailing list to mail boxes
								// if from address is valid member
								// also, check if message should be deleted
								// when forwarded -- should be a global setting
							}
						}
					}
				}
			}
		
			// here is where the mail is actually sent
			if((tickID==MudHost.TICK_EMAIL)
			&&(mailboxName()!=null)
			&&(mailboxName().length()>0))
			{
				Vector emails=CMClass.DBEngine().DBReadJournal(mailboxName());
				if(emails!=null)
				for(int e=0;e<emails.size();e++)
				{
					// **TODO check for forwarding, global AND individual
					Vector mail=(Vector)emails.elementAt(e);
					String key=(String)mail.elementAt(DatabaseEngine.JOURNAL_KEY);
					String from=(String)mail.elementAt(DatabaseEngine.JOURNAL_FROM);
					String to=(String)mail.elementAt(DatabaseEngine.JOURNAL_TO);
					long date=Util.s_long((String)mail.elementAt(DatabaseEngine.JOURNAL_DATE));
					String subj=((String)mail.elementAt(DatabaseEngine.JOURNAL_SUBJ)).trim();
					String msg=((String)mail.elementAt(DatabaseEngine.JOURNAL_MSG)).trim();
				}
			}
			lastAllProcessing=System.currentTimeMillis();
		}
		System.gc();
		try{Thread.sleep(1000);}catch(Exception ex){}
		tickStatus=STATUS_NOT;
		return true;
	}

	// interrupt does NOT interrupt the ServerSocket.accept() call...
	//  override it so it does
	public void interrupt()
	{
		if(servsock!=null)
		{
			try
			{
				servsock.close();
				//jef: we MUST set it to null
				// (so run() can tell it was interrupted & didn't have an error)
				servsock = null;
			}
			catch(IOException e)
			{
			}
		}
		super.interrupt();
	}

	public int getMaxMsgs()
	{
		String s=page.getStr("MAXMSGS");
		if(s==null) return Integer.MAX_VALUE;
		int x=Util.s_int(s);
		if(x==0) return Integer.MAX_VALUE;
		return x;
	}
	public long getMaxMsgSize()
	{
		String s=page.getStr("MAXMSGSIZE");
		if(s==null) return Long.MAX_VALUE;
		long x=Util.s_long(s);
		if(x==0) return Long.MAX_VALUE;
		return x;
	}
}