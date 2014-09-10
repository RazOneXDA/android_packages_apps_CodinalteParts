package com.meticulus.codinalteparts.app;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Created by meticulus on 4/7/14.
 */
public class FunctionsMain {

    private static final String TAG = "Codinalte Parts";

    /* General */
    private static final String TEMP_DIR_CMD = "mkdir -p /data/local/tmp";

    /* Sweep2wake */
    private static final String S2W_PATH = "/sys/module/sweep2wake/parameters";

    private static final String S2W_ENABLE_PATH = S2W_PATH + "/enable";

    private static final String S2W_WAKELOCK_PATH = S2W_PATH + "/use_wakelock";

    private static final String S2W_ENABLE_CMD = "echo 1 > " + S2W_ENABLE_PATH;

    private static final String S2W_DISABLE_CMD = "echo 0 > " + S2W_ENABLE_PATH;

    private static final String S2W_GET_ENABLE_CMD = "cat " + S2W_ENABLE_PATH;

    private static final String S2W_ENABLE_WAKELOCK_CMD = "echo 1 > " + S2W_WAKELOCK_PATH;

    private static final String S2W_DISABLE_WAKELOCK_CMD = "echo 0 > " + S2W_WAKELOCK_PATH;

    private static final String S2W_GET_WAKELOCK_CMD = "cat " + S2W_WAKELOCK_PATH;

    /* CPU2 Commands */
    private static final String CPU2_ONLINE_PATH = "/sys/devices/system/cpu/cpu1/online";

    private static final String CPU2_ENABLE_COMMAND = "echo 1 > " + CPU2_ONLINE_PATH;

    private static final String CPU2_DISABLE_COMMAND = "echo 0 >" + CPU2_ONLINE_PATH;

    /* Bluetooth Commands */
    private static final String CMD_BTPAN_IP = "RESULT=$(netcfg | grep bt-pan | awk -F '/' " +
            "'{print $1}' | awk -F ' ' '{print $ 3}'); if [[ \"$RESULT\" == \"\" ]]; " +
            "then echo \"not found\";else echo $RESULT;fi";

    private static final String CMD_BTPAN_DHCP = "netcfg bt-pan dhcp";

    private static final String CMD_DNS1 = "echo \"nameserver 8.8.8.8\" > /data/local/tmp/resolv.conf";

    private static final String CMD_DNS2 = "echo \"nameserver 8.8.4.4\" >> /data/local/tmp/resolv.conf";

    private static final String CMD_DNSMASQ = "dnsmasq -x /data/local/tmp/dnsmasq.pid -r /data/local/tmp/resolv.conf";

    /* Logging Vars */
    private static final String CMD_KMSG = "cat /proc/kmsg | while read LINE;do " + "" +
            "DATE=$(busybox date -I); echo \"$(busybox date | cut -d ' ' -f5) - $LINE\" >> "+
            "/sdcard/autolog_kmsg_\"$DATE\".txt;done &";

    private static final String CMD_LOGCAT = "logcat | while read LINE;do "+
            "DATE=$(busybox date -I); echo \"$(busybox date | cut -d ' ' -f4) - $LINE\" >> "+
            "/sdcard/autolog_logcat_\"$DATE\".txt;done &";

    private static final String CMD_RILLOG = "cat /dev/log/radio | while read LINE;do "+
            "DATE=$(busybox date -I); echo \"$(busybox date | cut -d ' ' -f4) - $LINE\" >> "+
            "/sdcard/autolog_rillog_\"$DATE\".txt;done &";

    /* Low Memory Killer - Not Killable Processes  */
    private static final String LMKNKP_ENABLE_SYSPROCS = "echo 1 > /sys/module/lowmemorykiller/" +
            "parameters/donotkill_sysproc";

    private static final String LMKNKP_DISABLE_SYSPROCS = "echo 0 > /sys/module/lowmemorykiller/" +
            "parameters/donotkill_sysproc";

    private static final String LMKNKP_PROC_LIST_START = "echo ";

    private static final String LMKNKP_PROC_LIST_TAIL = " > /sys/module/lowmemorykiller/"+
            "parameters/donotkill_sysproc_names";

    private static final String LMKNKP_PROC_LIST = "wpa_supplicant,rild,at_core,at_distributor";


    public static void setSweep2Wake(boolean enabled){
        try {
            if (enabled) {
                Log.i(TAG,"Enabling Sweep2wake");
                CommandUtility.ExecuteNoReturn(S2W_ENABLE_CMD, true);
            }else {
                Log.i(TAG,"Disabling Sweep2wake");
                CommandUtility.ExecuteNoReturn(S2W_DISABLE_CMD, true);
            }

            Log.i(TAG,"Sweep2Wake status: " + CommandUtility.ExecuteShellCommand(S2W_GET_ENABLE_CMD,
                    true).replace("\n",""));
        } catch (Exception ex){ex.printStackTrace();}


    }

    public static void startInCallAudioService(Context context)
    {
        Log.i(TAG,"Starting In Call Audio Service.");
        Intent intent = new Intent(context,InCallAudioService.class);
        context.startService(intent);
    }
    public static void startClockFreezeMonitorService(Context context)
    {
        Log.i(TAG,"Starting Clock Freeze Monitor Service.");
        Intent intent = new Intent(context,ClockFreezeMonitor.class);
        context.startService(intent);
    }

    public static void SetCPU2(Boolean enabled)
    {
        try{

            if(enabled)
            {
                Log.i(TAG,"Enabling CPU2");
                CommandUtility.ExecuteNoReturn(CPU2_ENABLE_COMMAND,true);
            }
            else
            {
                Log.i(TAG,"Disabling CPU2");
                CommandUtility.ExecuteNoReturn(CPU2_DISABLE_COMMAND, true);
            }
        }
        catch (Exception e){e.printStackTrace();}

    }

    public static Boolean areWeBtTetherClient()
    {
        Boolean retval = false;
        try{
            String btpanip = CommandUtility.ExecuteShellCommand(CMD_BTPAN_IP, true).replace("\n","");
            if(btpanip.equals("0.0.0.0"))
            {
                Log.i(TAG, "Found bt-pan ip " + btpanip);
                retval = true;
            }

        }
        catch (Exception e){e.printStackTrace();}
        return retval;

    }

    public static void runBTDHCP()
    {
        try{
            Log.i(TAG,"Running dhcp...");
            /*
            Try to make sure that the temporary dir exists first.
             */
            CommandUtility.ExecuteNoReturn(TEMP_DIR_CMD,true);
            CommandUtility.ExecuteNoReturn(CMD_BTPAN_DHCP, true);
        }
        catch (Exception e){e.printStackTrace();}
    } 
    public static void runBTDNSMasq()
    {
        try
        {
            /*
            Try to make sure that the temporary dir exists first.
             */
            CommandUtility.ExecuteNoReturn(TEMP_DIR_CMD,true);
            /*
            TCreate resolv.conf file.
             */
            CommandUtility.ExecuteNoReturn(CMD_DNS1,true);
            CommandUtility.ExecuteNoReturn(CMD_DNS2, true);
            Log.i(TAG, "Running dnsmasq...");
            CommandUtility.ExecuteNoReturn(CMD_DNSMASQ, true);
        }
        catch(Exception e){e.printStackTrace();}
    }

    public static void startAutokmsg()
    {
        try
        {
            Log.i(TAG, "Running auto kmsg...");
            CommandUtility.ExecuteNoReturn(CMD_KMSG,true);
        }
        catch(Exception e){e.printStackTrace();}
    }
    public static void startAutologcat()
    {
        try
        {
            Log.i(TAG, "Running auto logcat...");
            CommandUtility.ExecuteNoReturn(CMD_LOGCAT,true);
        }
        catch(Exception e){e.printStackTrace();}
    }
    public static void startAutorillog()
    {
        try
        {
            Log.i(TAG, "Running auto ril log...");
            CommandUtility.ExecuteNoReturn(CMD_RILLOG,true);
        }
        catch(Exception e){e.printStackTrace();}
    }

    public static void enableLMKNKP()
    {
        try
        {
            Log.i(TAG, "Enabling Not Killable Processes...");
            CommandUtility.ExecuteNoReturn(LMKNKP_ENABLE_SYSPROCS,true);
        }
        catch(Exception e){e.printStackTrace();}
    }
    public static void disableLMKNKP()
    {
        try
        {
            Log.i(TAG, "Disabling Not Killable Processes...");
            CommandUtility.ExecuteNoReturn(LMKNKP_DISABLE_SYSPROCS,true);
        }
        catch(Exception e){e.printStackTrace();}
    }

    public static void setLMKNKPWhitelist()
    {
        try
        {
            Log.i(TAG, "Setting Not Killable Processes List...");
            CommandUtility.ExecuteNoReturn(LMKNKP_PROC_LIST_START + LMKNKP_PROC_LIST +
                    LMKNKP_PROC_LIST_TAIL,true);
        }
        catch(Exception e){e.printStackTrace();}
    }

    public static void testProp()
    {
        Log.e(TAG, "Device is: " + Build.MODEL);
    }
}
