package br.com.alphasignage.servidorcaixalivre.Activity;


import android.content.BroadcastReceiver;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;

import android.os.Bundle;
import android.os.Looper;


import android.support.v7.app.AppCompatActivity;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import android.view.WindowManager;

import android.widget.Button;

import android.widget.TextView;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import br.com.alphasignage.servidorcaixalivre.Adapter.MyAdapter;
import br.com.alphasignage.servidorcaixalivre.BroadcastReceiver.WifiDirectBroadcastReceiver;
import br.com.alphasignage.servidorcaixalivre.R;

import br.com.alphasignage.servidorcaixalivre.Service.FileTransferService;
import br.com.alphasignage.servidorcaixalivre.Task.DataServerAsyncTask;

import br.com.alphasignage.servidorcaixalivre.Utils.Configuracao;
import br.com.alphasignage.servidorcaixalivre.Utils.Utils;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private Button begrouppwener;

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private List peers = new ArrayList();
    private List<HashMap<String, String>> peersshow = new ArrayList();

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;
    private WifiP2pInfo info;

    private TextView tvMain;

    private DataServerAsyncTask mDataTask;

    private Utils utils;
    //private Spinner spAtendimentos;
   // private boolean editandoCaixa = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
        initIntentFilter();
        initReceiver();
        initEvents();
        BeGroupOwener();
        registerReceiver(mReceiver, mFilter);

    }



    private void initEvents() {
        begrouppwener = findViewById(R.id.bt_bgowner);
        begrouppwener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeGroupOwener();
            }
        });
        tvMain = findViewById(R.id.tv_main);
    }


    private void BeGroupOwener() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                tvMain.setText("Grupo criado com sucesso.");
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 20) {
            super.onActivityResult(requestCode, resultCode, data);
            Uri uri = data.getData();
            Intent serviceIntent = new Intent(MainActivity.this,
                    FileTransferService.class);

            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
                    uri.toString());

            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                    8988);
            MainActivity.this.startService(serviceIntent);
        }
    }

    private void StopConnect() {
        TextView view = (TextView) findViewById(R.id.tv_main);
        view.setText("Desconectado");
        SetButtonGone();
        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    /*A demo base on API which you can connect android device by wifidirect,
    and you can send file or data by socket,what is the most important is that you can set
    which device is the client or service.*/


    private void StopDiscoverPeers() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {


            }
        });
    }

    private void initView() {

        mRecyclerView = findViewById(R.id.recyclerview);

        mAdapter = new MyAdapter(peersshow);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager
                (this.getApplicationContext()));

    }

    private void initReceiver() {
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, Looper.myLooper(), null);

        String nome = Configuracao.getDeviceName(getApplicationContext());
        if(nome.equals("")){ //o nome ainda não foi definido
            nome = "Caixa-Livre"+new Random().nextInt(99);
            Configuracao.setDeviceName(nome,getApplicationContext());
        }
        TextView tvNome = findViewById(R.id.tvNome);
        tvNome.setText(nome);

        try {
            Method method = WifiP2pManager.class.getMethod("setDeviceName",
                    WifiP2pManager.Channel.class, String.class, WifiP2pManager.ActionListener.class);

            method.invoke(mManager, mChannel, nome, new WifiP2pManager.ActionListener() {
                public void onSuccess() {}

                public void onFailure(int reason) {}
            });
        } catch (Exception e)   {}

        WifiP2pManager.PeerListListener mPeerListListerner = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peersList) {
                peers.clear();
                peersshow.clear();
                Collection<WifiP2pDevice> aList = peersList.getDeviceList();
                peers.addAll(aList);

                for (int i = 0; i < aList.size(); i++) {
                    WifiP2pDevice a = (WifiP2pDevice) peers.get(i);
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("name", a.deviceName);
                    map.put("address", a.deviceAddress);
                    peersshow.add(map);

                    //CreateConnect(a.deviceAddress,a.deviceName);
                }
                mAdapter = new MyAdapter(peersshow);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager
                        (MainActivity.this));

            }
        };



        WifiP2pManager.ConnectionInfoListener mInfoListener = new WifiP2pManager.ConnectionInfoListener() {

            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo minfo) {

                Log.i("xyz", "InfoAvailable is on: ");
                info = minfo;
                TextView view = (TextView) findViewById(R.id.tv_main);
                if (info.groupFormed && info.isGroupOwner) {
                    Log.i("xyz", "owmer start");

                 /*   mServerTask = new FileServerAsyncTask(MainActivity.this, view);
                    mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/

                   mDataTask = new DataServerAsyncTask(MainActivity.this, view);
                    mDataTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                   // sendData();

                } else if (info.groupFormed) {
                    view.setText("Conectado a: "+Configuracao.getDeviceName(getApplicationContext()));
                    SetButtonVisible();


                }
            }
        };
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this, mPeerListListerner, mInfoListener);
    }

    private void SetButtonVisible() {
        //sendpicture.setVisibility(View.VISIBLE);

       // senddata.setVisibility(VISIBLE);
    }

    private void SetButtonGone() {
       // sendpicture.setVisibility(View.GONE);
        //senddata.setVisibility(View.GONE);
    }


    private void DiscoverPeers() {
        mRecyclerView.setVisibility(VISIBLE);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
            }
        });
    }

    private void initIntentFilter() {
        mFilter = new IntentFilter();
        mFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("xyz", "hehehe");
       // StopConnect();

    }

    @Override
    protected void onStop() {
       // StopConnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        StopConnect();
        unregisterReceiver(mReceiver);
        super.onDestroy();

    }

    public void ResetReceiver() {

        unregisterReceiver(mReceiver);
        registerReceiver(mReceiver, mFilter);

    }
}
