package zkar.com.testnfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import java.lang.String;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    RadioButton read, write;
    private EditText writecon;
    private EditText sectorAddress;
    private EditText blockIndex;
    private EditText inputkey;
    private TextView readcon;
    byte[] a ={(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
    //byte[] a ={(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
//默认密码
    //byte[] testkey ={(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11};
    //默认密码
    String TAG = "MainActivity";
    private NfcAdapter defaultAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        read = findViewById(R.id.read);
        write = findViewById(R.id.write);
        writecon = findViewById(R.id.writecon);
        readcon = findViewById(R.id.readcon);
        sectorAddress = findViewById(R.id.sector);
        blockIndex = findViewById(R.id.block);
        inputkey = findViewById(R.id.keyab);


        // 获得Adapter对象
        defaultAdapter = NfcAdapter.getDefaultAdapter(this);
        if (defaultAdapter == null) {
            Toast.makeText(this, "设备不支持NFC！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!defaultAdapter.isEnabled()) {
            Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        read.setOnCheckedChangeListener(this);
        write.setOnCheckedChangeListener(this);
    }
    //只有执行了onNewIntent（）方法才会执行到以下读写方法
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(read.isChecked()){
            String data = readTag(tag);
            readcon.setText(new StringBuilder().append("读到的内容为：").append(data).toString());
        }else if(write.isChecked()){
            writeTag(tag,writecon.getText().toString());
        }
    }
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            //System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }

    public static String bytes2String(byte[] k) throws Exception {
        return new String(k, "UTF-8");

    }
    public static String fiilString(String g)  {
        String exa = "                ";
        String fin = g;
        int l1 = g.length();
        int l2 = exa.length();
        if(l2 == l1){
            return g;
        }else if (l2>l1){
            fin = (g + exa).substring(0,16);
            return fin;
        }else {
            fin = g.substring(0,16);
            return fin;
        }
    }
        public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] m = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            m[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return m;
    }
    public String readTag(Tag tag) {
        MifareClassic mfc = MifareClassic.get(tag);
        for (String tech : tag.getTechList()) {
            System.out.println(tech);
        }
        //boolean auth = false;
        boolean auth1 = false;
        boolean auth2 = false;
        boolean authb1 = false;
        boolean authb2 = false;

        byte[] b = hexStringToByteArray(inputkey.getText().toString());
        if(b.length == 0)
            b = a;
        //读取TAG
        try {
            String metaInfo = "";
            //Enable I/O operations to the tag from this TagTechnology object.
            mfc.connect();
            int type = mfc.getType();//获取TAG的类型
            int sectorCount = mfc.getSectorCount();//获取TAG中包含的扇区数
            String typeS = "";
            switch (type) {
                case MifareClassic.TYPE_CLASSIC:
                    typeS = "TYPE_CLASSIC";
                    break;
                case MifareClassic.TYPE_PLUS:
                    typeS = "TYPE_PLUS";
                    break;
                case MifareClassic.TYPE_PRO:
                    typeS = "TYPE_PRO";
                    break;
                case MifareClassic.TYPE_UNKNOWN:
                    typeS = "TYPE_UNKNOWN";
                    break;
            }
            metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共"
                    + mfc.getBlockCount() + "个块\n存储空间: " + mfc.getSize()
                    + "B\n";
            for (int j = 0; j < sectorCount; j++) {
                //Authenticate a sector with key A.
                auth1 = mfc.authenticateSectorWithKeyA(j, a); //|| mfc.authenticateSectorWithKeyA(j,b));
                // auth2 = mfc.authenticateSectorWithKeyA(j,b);
                //auth2 = mfc.authenticateSectorWithKeyB(j,testkey);
                int bCount;
                int bIndex;
                if (auth1) {
                    metaInfo += "Sector " + j + ":验证成功\n";
                    // 读取扇区中的块[0xFF,0xFF,0xFF,0xFF,0xFF,0xFF]MifareClassic.KEY_NFC_FORUM MifareClassic.KEY_DEFAULT
                    bCount = mfc.getBlockCountInSector(j);
                    bIndex = mfc.sectorToBlock(j);
                    for (int i = 0; i < bCount; i++) {
                        byte[] data = mfc.readBlock(bIndex);
                        metaInfo += "Block " + bIndex + " : "
                                + bytesToHexString(data) + "\n" + "对应的ascii码为：" + bytes2String(data) + "\n";
                        bIndex++;
                    }
                } else {
                    //auth2 = mfc.authenticateSectorWithKeyA(j,testkey);
                    auth2 = mfc.authenticateSectorWithKeyA(j, b);

                    if (auth2) {
                        metaInfo += "Sector " + j + ":自定义密码A验证成功\n";
                        bCount = mfc.getBlockCountInSector(j);
                        bIndex = mfc.sectorToBlock(j);
                        for (int i = 0; i < bCount; i++) {
                            byte[] data = mfc.readBlock(bIndex);
                            metaInfo += "Block " + bIndex + " : "
                                    + bytesToHexString(data) + "\n" + "对应的ascii码为：" + bytes2String(data) + "\n";
                            bIndex++;
                        }
                    } else {
                        authb1 = mfc.authenticateSectorWithKeyB(j, a);

                        if (authb1) {
                            metaInfo += "Sector " + j + ":默认密码B验证成功\n";
                            bCount = mfc.getBlockCountInSector(j);
                            bIndex = mfc.sectorToBlock(j);
                            for (int i = 0; i < bCount; i++) {
                                byte[] data = mfc.readBlock(bIndex);
                                metaInfo += "Block " + bIndex + " : "
                                        + bytesToHexString(data) + "\n" + "对应的ascii码为：" + bytes2String(data) + "\n";
                                bIndex++;
                            }
                        } else {
                            authb2 = mfc.authenticateSectorWithKeyB(j, b);

                            if (authb2) {
                                metaInfo += "Sector " + j + ":自定义密码B验证成功\n";
                                bCount = mfc.getBlockCountInSector(j);
                                bIndex = mfc.sectorToBlock(j);
                                for (int i = 0; i < bCount; i++) {
                                    byte[] data = mfc.readBlock(bIndex);
                                    metaInfo += "Block " + bIndex + " : "
                                            + bytesToHexString(data) + "\n" + "对应的ascii码为：" + bytes2String(data) + "\n";
                                    bIndex++;
                                }
                            } else {
                                metaInfo += "Sector " + j + ":验证失败\n";
                            }
                        }
                    }
                }
            }
            return metaInfo;
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (mfc != null) {
                try {
                    mfc.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
        return null;
    }
    public void writeTag(Tag tag,String writecon) {
        MifareClassic mfc = MifareClassic.get(tag);
        byte[] z = hexStringToByteArray(inputkey.getText().toString());
        if (z.length == 0)
            z = a;
        try {
            mfc.connect();
            boolean auth3 = false;
            boolean auth4 = false;
            boolean authb3 = false;
            boolean authb4 = false;
            int x = Integer.parseInt(sectorAddress.getText().toString());
            int y = Integer.parseInt(blockIndex.getText().toString());
            //short sectorAddress = 1;//扇区是0-15 sectorAddress 为1写入的是第二扇区
            //验证密码 验证第一扇区的默认keyA
            auth3 = mfc.authenticateSectorWithKeyA(x,
                    a);
            /*
              验证密码
              blockIndex 代表的是第几块
              writecon 要写入的内容
              写入时要写入16个字节，写入时添加"abcdefghijkl"，写入成功后读取到写入数据时方便截取
             */
            if (auth3) {
                // the last block of the sector is used for KeyA and KeyB cannot be overwritted
                //mfc.writeBlock(4, ("abcdefghijkl"+writecon).getBytes());
                mfc.writeBlock(y, fiilString(writecon).getBytes());
                //mfc.writeBlock(blockIndex, writecon.getBytes());
                readcon.setText(new StringBuilder().append("写入的内容为：").append(bytesToString((fiilString(writecon)).getBytes())).append("\n").append("卡内实际存储的内容为：").append("\n").append(bytesToHexString((fiilString(writecon)).getBytes())).append(".size=").append((fiilString(writecon)).getBytes().length).toString());
                mfc.close();
                Toast.makeText(this, "使用默认密码A写入成功", Toast.LENGTH_SHORT).show();
            } else {
                auth4 = mfc.authenticateSectorWithKeyA(x, z);
                if (auth4) {
                    // the last block of the sector is used for KeyA and KeyB cannot be overwritted
                    //mfc.writeBlock(4, ("abcdefghijkl"+writecon).getBytes());
                    mfc.writeBlock(y, fiilString(writecon).getBytes());
                    //mfc.writeBlock(blockIndex, writecon.getBytes());
                    readcon.setText(new StringBuilder().append("写入的内容为：").append(bytesToString((fiilString(writecon)).getBytes())).append("\n").append("卡内实际存储的内容为：").append("\n").append(bytesToHexString((fiilString(writecon)).getBytes())).append(".size=").append((fiilString(writecon)).getBytes().length).toString());
                    mfc.close();
                    Toast.makeText(this, "使用自定义密码A写入成功", Toast.LENGTH_SHORT).show();
                } else {
                    authb3 = mfc.authenticateSectorWithKeyB(x, a);
                    if (authb3) {
                        // the last block of the sector is used for KeyA and KeyB cannot be overwritted
                        //mfc.writeBlock(4, ("abcdefghijkl"+writecon).getBytes());
                        mfc.writeBlock(y, fiilString(writecon).getBytes());
                        //mfc.writeBlock(blockIndex, writecon.getBytes());
                        readcon.setText(new StringBuilder().append("写入的内容为：").append(bytesToString((fiilString(writecon)).getBytes())).append("\n").append("卡内实际存储的内容为：").append("\n").append(bytesToHexString((fiilString(writecon)).getBytes())).append(".size=").append((fiilString(writecon)).getBytes().length).toString());
                        mfc.close();
                        Toast.makeText(this, "使用默认密码B写入成功", Toast.LENGTH_SHORT).show();

                    } else {
                        authb4 = mfc.authenticateSectorWithKeyB(x, z);
                        if (authb4) {
                            // the last block of the sector is used for KeyA and KeyB cannot be overwritted
                            //mfc.writeBlock(4, ("abcdefghijkl"+writecon).getBytes());
                            mfc.writeBlock(y, fiilString(writecon).getBytes());
                            //mfc.writeBlock(blockIndex, writecon.getBytes());
                            readcon.setText(new StringBuilder().append("写入的内容为：").append(bytesToString((fiilString(writecon)).getBytes())).append("\n").append("卡内实际存储的内容为：").append("\n").append(bytesToHexString((fiilString(writecon)).getBytes())).append(".size=").append((fiilString(writecon)).getBytes().length).toString());
                            mfc.close();
                            Toast.makeText(this, "使用自定义密码B写入成功", Toast.LENGTH_SHORT).show();


                        } else {
                            Toast.makeText(this, "密码全部写入失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }




        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                mfc.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    private String bytesToString(byte[] src){
        String s = new String(src);
        String removeStr = "abcdefghijkl";
        return s.replace(removeStr,"");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (defaultAdapter != null)
            defaultAdapter.enableForegroundDispatch(this, mPendingIntent, null,
                    null);
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.read:
                read.setChecked(b);
                write.setChecked(!b);
                break;
            case R.id.write:
                write.setChecked(b);
                read.setChecked(!b);
                //readcon.setText("");
                //writecon.setText("");
                //sectorAddress.setText("");
                //blockIndex.setText("");
                break;

        }
    }

}
