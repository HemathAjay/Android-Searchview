package hemath.com.searchview.custom;

import android.content.*;
import android.os.*;
import android.support.v7.app.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;


import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.*;

import hemath.com.search.MaterialSearchBar;
import hemath.com.searchview.*;
import hemath.com.searchview.Config;
import hemath.com.searchview.R;

import java.util.*;

public class CustomAdapterActivity extends AppCompatActivity implements View.OnClickListener, MaterialSearchBar.OnSearchActionListener {
    private MaterialSearchBar searchBar;
    private List<Product> suggestions = new ArrayList<>();
    private CustomSuggestionsAdapter customSuggestionsAdapter;
    private String search_QUERY;
    public RequestQueue requestQueue;
    private int requestCount = 1;

    private Context mContext;
    // Sample data
    private final String[] products = {
        "Simvastatin",
        "Carrot Daucus carota",
        "Sodium Fluoride",
        "White Kidney Beans",
        "Salicylic Acid",
        "cetirizine hydrochloride",
        "Mucor racemosus",
        "Thymol",
        "TOLNAFTATE",
        "Albumin Human"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_adapter);
        mContext = this;
        Config.customAdapterActivity = this;

        searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        customSuggestionsAdapter = new CustomSuggestionsAdapter(inflater,mContext);



        requestQueue = Volley.newRequestQueue(mContext);
        search_QUERY = "Android";
        getData();

        Button addProductBtn = (Button) findViewById(R.id.button);
        addProductBtn.setOnClickListener(this);

        searchBar.setMaxSuggestionCount(2);
        searchBar.setHint("Find Product..");

//        for (int i = 1; i < 11; i++) {
//            suggestions.add(new Product(products[i -1], products[i -1]));
//        }

        customSuggestionsAdapter.setSuggestions(suggestions);
        searchBar.setCustomSuggestionAdapter(customSuggestionsAdapter);

        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("LOG_TAG", getClass().getSimpleName() + " text changed " + searchBar.getText());
                // send the entered text to our filter and let it manage everything
                search_QUERY = searchBar.getText();
                getData();
                customSuggestionsAdapter.getFilter().filter(searchBar.getText());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
    }

    @Override
    public void onClick(View view) {
        customSuggestionsAdapter.addSuggestion(new Product("Title", "Author"));
    }

    private void getData()
    {
        requestQueue.add(getDataFromServer(requestCount));
        requestCount++;
    }

    public void setSearchText(String text)
    {
        searchBar.setText(text);
    }

    private StringRequest getDataFromServer(int requestCount)
    {

        String Book_URL =Config.DATA_URL + String.valueOf(requestCount)+"&sUserId=0&sDevice="+search_QUERY;


        StringRequest jsonArrayRequest = new StringRequest(Book_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response)
                    {
                        try
                        {
                            if (response.isEmpty()){
                                Toast.makeText(getBaseContext(), "No more books available", Toast.LENGTH_LONG).show();
                            }
                            else{
                                JSONArray Jarray = new JSONArray(response);
                                new DisplayHandler().execute(Jarray);
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String ResError = error.getMessage();
                        Toast.makeText(CustomAdapterActivity.this, ResError, Toast.LENGTH_SHORT).show();
                    }
                });
        return jsonArrayRequest;
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {

    }

    @Override
    public void onSearchConfirmed(CharSequence text) {

    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
               // drawer.openDrawer(Gravity.LEFT);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                break;
            case MaterialSearchBar.BUTTON_BACK:
                searchBar.disableSearch();
                break;
            case MaterialSearchBar.BUTTON_INTENT_BACK:
                Intent go = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(go);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private class DisplayHandler extends AsyncTask<JSONArray, String, String>
    {
        String msg="";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(JSONArray... data)
        {
            String  title = "", image_url = "" , author = "";

            try {
                JSONArray array = data[0];
                for (int i = 0; i < array.length(); i++)
                {
                    try {
                        JSONObject json = array.getJSONObject(i);
                        title = json.getString(Config.TAG_TITLE);
                        image_url = json.getString(Config.TAG_IMAGE_URL);
                        image_url = image_url.replace("http://","https://");
                        author = json.getString(Config.TAG_AUTHOR_NAME);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    suggestions.add(new Product(title,author));
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            customSuggestionsAdapter.notifyDataSetChanged ();
            if(searchBar.isSearchEnabled() == false)
            {
                searchBar.enableSearch();
            }
        }
    }

}
