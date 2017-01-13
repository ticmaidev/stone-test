package br.com.stonesdk.sdkdemo;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import stone.application.StoneStart;
import stone.application.interfaces.StoneCallbackInterface;
import stone.providers.ActiveApplicationProvider;
import stone.user.Partner;
import stone.user.UserModel;
import stone.utils.GlobalInformations;
import stone.utils.Stone;

public class ValidationActivity extends ActionBarActivity {

    private final int SPLASH_DISPLAY_LENGTH = 3500;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_validation);

		/* Este deve ser, obrigatoriamente, o primeiro metodo
		 * a ser chamado. E um metodo que trabalha com sessao.
		 */
        List<UserModel> user = StoneStart.init(this);

        // se retornar nulo, voce provavelmente nao ativou a SDK
        // ou as informacoes da Stone SDK foram excluidas
        if (user == null) {

            List<String> stoneCodeList = new ArrayList<>();
            // Adicione seu Stonecode abaixo, como string.
            stoneCodeList.add("SEU STONE CODE AQUI");

            ActiveApplicationProvider activeApplicationProvider = new ActiveApplicationProvider(this, stoneCodeList);
            activeApplicationProvider.setDialogMessage("Ativando o aplicativo...");
            activeApplicationProvider.setDialogTitle("Aguarde");
            activeApplicationProvider.setActivity(ValidationActivity.this);
            activeApplicationProvider.setWorkInBackground(false); // informa se este provider ira rodar em background ou nao
            activeApplicationProvider.setConnectionCallback(new StoneCallbackInterface() {

				/* Sempre que utilizar um provider, intancie esta Interface.
				 * Ela ira lhe informar se o provider foi executado com sucesso ou nao
				 */

                /* Metodo chamado se for executado sem erros */
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Ativado com sucesso, iniciando o aplicativo", Toast.LENGTH_SHORT).show();
                    continueApplication();
                }

                /* metodo chamado caso ocorra alguma excecao */
                public void onError() {
                    Toast.makeText(getApplicationContext(), "Erro na ativacao do aplicativo, verifique a lista de erros do provider", Toast.LENGTH_SHORT).show();
					/* Chame o metodo abaixo para verificar a lista de erros. Para mais detalhes, leia a documentacao:
					   activeApplicationProvider.getListOfErrors(); */
                }
            });
            activeApplicationProvider.execute();
        } else {

			/* caso ja tenha as informacoes da SDK e chamado o ActiveApplicationProvider anteriormente
			   sua aplicacao podera seguir o fluxo normal */
            continueApplication();

        }
    }

    private void continueApplication(){

        new Handler().postDelayed(new Runnable() {
            public void run() {

                // habilita o modo desenvolvedor
                Stone.developerMode();

                Intent mainIntent = new Intent(ValidationActivity.this, MainActivity.class);
                ValidationActivity.this.startActivity(mainIntent);
                ValidationActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
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

