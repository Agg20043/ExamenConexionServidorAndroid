package com.example.examenconexionservidorandroid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listViewUsuarios;
    ArrayList<JSONObject> listaUsuarios;
    ArrayAdapter<JSONObject> adapter;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Click del MenuItem CRUD
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu) {
            Intent intent = new Intent(this, CRUD.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewUsuarios = findViewById(R.id.listView);
        listaUsuarios = new ArrayList<>();

        adapter = new ArrayAdapter<JSONObject>(this, android.R.layout.simple_list_item_1, listaUsuarios) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                }
                TextView textView = convertView.findViewById(android.R.id.text1);
                JSONObject usuario = getItem(position);
                try {
                    String nombreUsuario = usuario.getString("USER");
                    textView.setText("Usuario: " + nombreUsuario);
                } catch (JSONException e) {
                    e.printStackTrace();
                    textView.setText("Error");
                }
                return convertView;
            }
        };

        listViewUsuarios.setAdapter(adapter);
    //Cuando hagas click en un usuario te muestra la contraseña.
        listViewUsuarios.setOnItemClickListener((parent, view, position, id) -> {
            //Agarra el objeto JSON de el usuario
            JSONObject usuario = adapter.getItem(position);
            try {
                String contrasena = usuario.getString("PASSWORD");
                Toast.makeText(MainActivity.this, "Contraseña: " + contrasena, Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        cargarUsuarios();
    }


//Carga los usuarios que hay en el servidor actualmente
    private void cargarUsuarios() {
        new FetchDataFromServer().execute("http://192.168.56.1/loginuser/getTabla.php");
    }

    //Obtiene los datos desde el servidor
    private class FetchDataFromServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder resultado = new StringBuilder();
            HttpURLConnection conexion = null;
            try {
                URL url = new URL(urls[0]);
                conexion = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(conexion.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String linea;
                while ((linea = reader.readLine()) != null) {
                    resultado.append(linea);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "Error de conexión: " + e.getMessage();
            } finally {
                if (conexion != null) {
                    conexion.disconnect();
                }
            }
            return resultado.toString();
        }

        @Override
        protected void onPostExecute(String resultado) {
            super.onPostExecute(resultado);
            if (!resultado.startsWith("Error")) {
                try {
                    //Obtiene una cadena JSON a un array de objetos JSON
                    JSONArray jsonArray = new JSONArray(resultado);
                    //Limpiar la lista de usuarios que hay y agrega a los nuevos.
                    listaUsuarios.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject usuario = jsonArray.getJSONObject(i);
                        listaUsuarios.add(usuario);
                    }
                    //Notifica los cambios
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}