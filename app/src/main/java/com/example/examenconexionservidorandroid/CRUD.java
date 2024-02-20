package com.example.examenconexionservidorandroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class CRUD extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crud);

        Button btnAgregarUsuario = findViewById(R.id.btnAgregarUsuario);
        Button btnModificarContrasena = findViewById(R.id.btnModificarContrasena);
        Button btnEliminarUsuario = findViewById(R.id.btnEliminarUsuario);
        Button btnEliminarTodosUsuarios = findViewById(R.id.btnEliminarTodosUsuarios);

        btnModificarContrasena.setOnClickListener(v -> mostrarDialogoCambiarContrasena());
        btnEliminarTodosUsuarios.setOnClickListener(v -> eliminarTodosUsuarios());
        btnAgregarUsuario.setOnClickListener(v -> mostrarDialogoInsertarUsuario());
        btnEliminarUsuario.setOnClickListener(v -> mostrarDialogoEliminarUsuario());

    }

    //Metodo que elimina todos los usuarios.
    private void eliminarTodosUsuarios() {
        new EliminarTodosUsuariosTask().execute();
    }


    private class EliminarTodosUsuariosTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("http://192.168.56.1/loginuser/deleteAllUser.php");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.getOutputStream().close();

                // Leer la respuesta del servidor
                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString(); // Retorna la respuesta del servidor
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(CRUD.this, result, Toast.LENGTH_LONG).show();
            startActivity(new Intent(CRUD.this, MainActivity.class));
            finish();
        }
    }
    //Dialogo para insertar un usuario
    private void mostrarDialogoInsertarUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CRUD.this);
        builder.setTitle("Insertar nuevo usuario");
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_insertar_usuario,null);
        builder.setView(customLayout);
        builder.setPositiveButton("Insertar", (dialog, which) -> {

            EditText editTextUsuario = customLayout.findViewById(R.id.editTextUsuario);
            EditText editTextPassword = customLayout.findViewById(R.id.editTextPassword);

            String usuario = editTextUsuario.getText().toString();
            String password = editTextPassword.getText().toString();
            new AgregarUsuarioTask().execute(usuario, password);
            startActivity(new Intent(CRUD.this, MainActivity.class));

        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    //Agrega al usuario en el servidor
    private class AgregarUsuarioTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String requestURL = "http://192.168.56.1/loginuser/addUser.php?USER=" + params[0] + "&PASSWORD=" + params[1];
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(requestURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                } else {

                    //Muestra el codigo de error.
                    response.append("Error en la respuesta del servidor. Código: ").append(responseCode);
                }

                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                response.append("Excepción: ").append(e.getMessage());
            }

            return response.toString();
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(CRUD.this, result, Toast.LENGTH_SHORT).show();

        }
    }

    //Dialogo que eliminar usuario
    private void mostrarDialogoEliminarUsuario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CRUD.this);
        builder.setTitle("Eliminar Usuario");
        final EditText input = new EditText(CRUD.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombreUsuario = input.getText().toString();
                eliminarUsuario(nombreUsuario);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
//Metodo de eliminar
    private void eliminarUsuario(String usuario) {
        new BorrarUsuarioTask().execute(usuario);
        startActivity(new Intent(CRUD.this, MainActivity.class));
        finish();
    }

    //Elimina el usuario del servidor
    private class BorrarUsuarioTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String requestURL = "http://192.168.56.1/loginuser/deleteUser.php?USER=" + params[0];
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(requestURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //Lee la respuesta del servidor
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                } else {
                    response.append("Error en la respuesta del servidor. Código: ").append(responseCode);
                }

                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                response.append("Excepción: ").append(e.getMessage());
            }

            return response.toString();
        }
        @Override
        protected void onPostExecute(String result) {


            Toast.makeText(CRUD.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    //Dialogo de cambiar la contraseña
    private void mostrarDialogoCambiarContrasena() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CRUD.this);
        builder.setTitle("Cambiar Contraseña");
        final EditText inputUsuario = new EditText(CRUD.this);
        inputUsuario.setInputType(InputType.TYPE_CLASS_TEXT);
        inputUsuario.setHint("Nombre de usuario");
        builder.setView(inputUsuario);
        builder.setPositiveButton("Siguiente", (dialog, which) -> {
            String nombreUsuario = inputUsuario.getText().toString();
         mostrarDialogoCambiarContrasenaDetalle(nombreUsuario);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    //Dialogo para escribir la contraseña actual y poner la nueva para realizar al cambio correctamente
    private void mostrarDialogoCambiarContrasenaDetalle(String nombreUsuario) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CRUD.this);
        builder.setTitle("Cambiar Contraseña para " + nombreUsuario);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_cambiar_contrasena, null);
        builder.setView(customLayout);
        builder.setPositiveButton("Cambiar", (dialog, which) -> {
            EditText editTextContrasenaActual = customLayout.findViewById(R.id.editTextContrasenaActual);
            EditText editTextContrasenaNueva = customLayout.findViewById(R.id.editTextContrasenaNueva);
            String contrasenaActual = editTextContrasenaActual.getText().toString();
            String contrasenaNueva = editTextContrasenaNueva.getText().toString();
            cambiarContrasena(nombreUsuario, contrasenaActual, contrasenaNueva);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    //Metodo que llama al servidor y luego te devuelve al MainActivity.
    private void cambiarContrasena(String usuario, String contrasenaActual, String contrasenaNueva) {
        new CambiarContraseñaTask().execute(usuario, contrasenaActual, contrasenaNueva);
        startActivity(new Intent(CRUD.this, MainActivity.class));
        finish();
    }

    //Metodo que hace el cambio en el servidor y luego el servidor lee la respuesta en caso de exito si no te mostrara el error.
    private class CambiarContraseñaTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String requestURL = "http://192.168.56.1/loginuser/modifyUser.php?USER=" + params[0] +
                    "&PASSWORD=" + params[1] + "&NEWPASSWORD=" + params[2];
            StringBuilder response = new StringBuilder();

            try {
                URL url = new URL(requestURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                } else {
                    response.append("Error en la respuesta del servidor. Código: ").append(responseCode);
                }

                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                response.append("Excepción: ").append(e.getMessage());
            }

            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {

            Toast.makeText(CRUD.this, result, Toast.LENGTH_SHORT).show();
        }


    }
}
