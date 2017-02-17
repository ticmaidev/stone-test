package br.com.stonesdk.sdkdemo.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import br.com.stonesdk.sdkdemo.R;
import stone.application.interfaces.StoneCallbackInterface;
import stone.providers.SendEmailProvider;
import stone.utils.EmailClient;

public class SendEmailActivity extends ActionBarActivity {

    TextView userText;
    TextView sendText;
    EditText userEditText;
    EditText sendEmailText;
    Button sendButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);
        userText = (TextView) findViewById(R.id.emailDescription);
        sendText = (TextView) findViewById(R.id.emailSendDescription);
        userEditText = (EditText) findViewById(R.id.emailUserText);
        sendEmailText = (EditText) findViewById(R.id.emailSendText);
        sendButton = (Button) findViewById(R.id.sendEmailButton);

        instanceEvents();
    }

    private void instanceEvents() {
        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (isNetworkAvailable() == true) {

                    // captura o texto inserido pelo usuario
                    String emailTextSample = userEditText.getText().toString();
                    String emailTo = sendEmailText.getText().toString().trim();

                    EmailClient emailClient = new EmailClient("smtp.empresa.com",    // Servidor smtp.
                                                              "noreply@empresa.com", // Endereco de email (por exemplo, o seu noreply).
                                                              "123456",              // Senha do email.
                                                               emailTo,              // Email do destinatário.
                                                              "TÍTULO DO E-MAIL");   // Título do email a ser enviado.
                    emailClient.setSport("999"); // S Port
                    emailClient.setSmtpPport("999"); // SMTP P Port
                    String receipt = emailTextSample; // Texto digitado pelo usuario.

                    SendEmailProvider sendEmailProvider = new SendEmailProvider(SendEmailActivity.this, emailClient, receipt);
                    sendEmailProvider.setWorkInBackground(false); // Verifica se vai existir feedback ao usuario ou nao.
                    sendEmailProvider.setDialogMessage("Enviando comprovante");
                    sendEmailProvider.setConnectionCallback(new StoneCallbackInterface() {
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Enviado com sucesso", Toast.LENGTH_LONG).show();
                        }

                        public void onError() {
                            Toast.makeText(getApplicationContext(), "Nao enviado", Toast.LENGTH_LONG).show();
                        }
                    });
                    sendEmailProvider.execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Não há conexão com internet ativada.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Checa se existe alguma conexao com a internet para enviar o email.
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

