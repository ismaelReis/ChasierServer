package br.com.alphasignage.servidorcaixalivre.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import br.com.alphasignage.servidorcaixalivre.R;

/**
 * Created by Escalar Comunicação on 17/04/2018.
 */

public class Configuracao {
    public static String deviceName = "";//será a chaver de conexão.
    public static String num_caixa = "";

    public static void setDeviceName(String deviceName,Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.deviceName), deviceName);
        editor.apply();
        Configuracao.deviceName=deviceName;
    }

    public static String getDeviceName(Context context){
        if(!deviceName.equals(""))
            return deviceName;
        else{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String defaultValue = "aff";
            String deviceName = sharedPref.getString(context.getString(R.string.deviceName), defaultValue);
            Configuracao.deviceName=deviceName;
            return "";
        }

    }
    public static void setNum_caixa(String num_caixa,Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        //editor.putString(context.getString(R.string.num_caixa), num_caixa);
        editor.apply();
        Configuracao.num_caixa=num_caixa;
    }

    public static String getNum_caixa(Context context){
        if(!num_caixa.equals(""))
            return num_caixa;
        else{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String defaultValue = "1";
            //String num_caixa = sharedPref.getString(context.getString(R.string.num_caixa), defaultValue);
            Configuracao.num_caixa=num_caixa;
            return num_caixa;
        }

    }
}
