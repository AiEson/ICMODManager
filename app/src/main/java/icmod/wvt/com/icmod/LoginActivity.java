package icmod.wvt.com.icmod;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import icmod.wvt.com.icmod.others.Algorithm;
import icmod.wvt.com.icmod.others.FinalValuable;

public class LoginActivity extends AppCompatActivity {
    EditText email, passwd;
    Button loginButton;
    TextView loginMessage, signIn;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = findViewById(R.id.input_email);
        passwd = findViewById(R.id.input_password);
        linearLayout = findViewById(R.id.login_layout);
        loginMessage = findViewById(R.id.login_message);
        signIn = findViewById(R.id.link_signup);
        loginButton = findViewById(R.id.btn_login);
        loginMessage.setVisibility(View.GONE);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailStr = Algorithm.getStringNoBlank(email.getText().toString().trim());
                String passwdStr = Algorithm.getStringNoBlank(passwd.getText().toString().trim());
                if (emailStr.length() != 0 && passwdStr.length() != 0)
                {
                    if (emailStr.length() > 5 && passwdStr.length() > 5) {
                        if (Algorithm.emailFormat(emailStr)) {
                            post_task postTask = new post_task(emailStr, passwdStr);
                            postTask.execute();
                        } else {
                            print("格式错误，请正确输入您的邮箱", Snackbar.LENGTH_LONG);
                        }
                    }
                    else {
                        print("您输入的账户和密码是不是有点短了呢？", Snackbar.LENGTH_LONG);
                    }
                }
                else{
                    print("请输入您的账户和密码", Snackbar.LENGTH_LONG);
                }
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://adodoz.cn/wp-login.php?action=register")
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    class post_task extends AsyncTask<Void, Void, JSONObject> {
        String email;
        String passwd;
        ProgressDialog progressDialog;
        post_task(String email, String passwd)
        {
            this.email = email;
            this.passwd = passwd;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this);
            progressDialog.setMessage("正在验证您的账户，请稍后...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            String ret2 = null;
            String Resultms = null;
            String lines;
            StringBuffer response = new StringBuffer("");
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://adodoz.cn/app_login.php");
                //连接服务器
                connection = (HttpURLConnection) url.openConnection();
                //上传服务器内容
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(8000);
                connection.setDoInput(true);//允许输入
                connection.setDoOutput(true);//允许输出
                connection.setUseCaches(false);
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.connect();
                DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                outStream.writeBytes("username=" + email + "&password=" + passwd + "&type=" + "email");
                outStream.flush();
                outStream.close();
                //读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                //读数据
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    response.append(lines);
                }
                ret2 = response.toString().trim();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            Log.e("TAG", ret2);
            JSONObject retJson = null;
            try {
                retJson = new JSONObject(ret2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return retJson;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            progressDialog.dismiss();
            if (jsonObject.isNull("code"))
            {
                JSONObject userInfo = null;
                try {
                    userInfo = jsonObject.getJSONObject("user_info");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    Toast.makeText(LoginActivity.this, "登陆成功，欢迎您 " + userInfo.getString("user_name") , Toast.LENGTH_LONG).show();
                    Algorithm.writeFile(FinalValuable.UserInfo, jsonObject.toString());
                    MainActivity.messageCallback.onMessage("ok");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LoginActivity.this.finish();
            }
        }
    }

    public void print(String string, int longg) {
        Snackbar.make(linearLayout, string, longg)
                .setAction("Action", null).show();
    }
}
