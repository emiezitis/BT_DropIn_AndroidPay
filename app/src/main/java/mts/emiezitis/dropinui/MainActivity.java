package mts.emiezitis.dropinui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.LineItem;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private AsyncHttpClient client = new AsyncHttpClient();
    private static final String SERVER_BASE = "http://your server-side URL";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void onBraintreeSubmit(View v) {

        DropInRequest dropInRequest = new DropInRequest()
                .tokenizationKey("your_sandbox_tokenizationKey")
                .androidPayCart(getAndroidPayCart());

        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);

                //send payment_method_nonce to server
                RequestParams params = new RequestParams();
                params.put("payment_method_nonce", result.getPaymentMethodNonce().getNonce());
                params.put("amount", "1.00");

                client.post(SERVER_BASE + "/payment", params, new TextHttpResponseHandler() {


                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Error: " + responseString);
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {

                        Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Success: " + responseString);

                    }


                });
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG,"Payment flow canceled by user");
            } else {
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
                Log.d(TAG,"There was an Exception, Error: " + error.toString());
            }
        }
    }


// Build a Cart object and provide it at the time of payment.

    private Cart getAndroidPayCart() {
        return Cart.newBuilder()
                .setCurrencyCode("USD")
                .setTotalPrice("1.00")
                .addLineItem(LineItem.newBuilder()
                        .setCurrencyCode("USD")
                        .setDescription("Android Pay")
                        .setQuantity("1")
                        .setUnitPrice("1.00")
                        .setTotalPrice("1.00")
                        .build())
                .build();
    }



}
