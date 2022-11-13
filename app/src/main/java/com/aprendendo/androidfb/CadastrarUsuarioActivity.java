package com.aprendendo.androidfb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CadastrarUsuarioActivity extends AppCompatActivity {
    private EditText edtNomeCadastro, edtEmailCadastro, edtSenhaCadastro, edtConfirmarSenhaCadastro;
    private Button btnCadastrar;
    private TextView tvPossuiConta;
    private String[] mensagens = {"Preencha todos os campos!", "Cadastro realizado com sucesso!"};
    private String usuarioID, nome, email, senha, confirmarSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_usuario);

        setTitle("Cadastrar Usuário");
        iniciaComponentes();
        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtNomeCadastro.getText().toString().isEmpty()||
                        edtEmailCadastro.getText().toString().isEmpty() ||
                        edtSenhaCadastro.getText().toString().isEmpty() ||
                        edtConfirmarSenhaCadastro.getText().toString().isEmpty()){
                    Snackbar snackbar = Snackbar.make(view, mensagens[0], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();

                }else{
                    if (edtSenhaCadastro.getText().toString().equals(edtConfirmarSenhaCadastro.getText().toString())){
                        cadastrarUsuario(view);
                    }
                    else{
                        Snackbar snackbar = Snackbar.make(view, "As senhas não coincidem.", Snackbar.LENGTH_SHORT);
                        snackbar.setBackgroundTint(Color.WHITE);
                        snackbar.setTextColor(Color.BLACK);
                        snackbar.show();
                    }
                }
            }});
        tvPossuiConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                telaLogin();
            }
        });

    }

    private void iniciaComponentes() {
        //Mapeia os componentes do front para o back-end.
        edtNomeCadastro = findViewById(R.id.edtCNome);
        edtEmailCadastro = findViewById(R.id.edtCEmail);
        edtSenhaCadastro = findViewById(R.id.edtCSenha);
        edtConfirmarSenhaCadastro = findViewById(R.id.edtCConfirmarSenha);
        btnCadastrar = findViewById(R.id.btnCCadastrar);
        tvPossuiConta = findViewById(R.id.tvPossuiConta);
    }

    private void cadastrarUsuario(View view){
        email = edtEmailCadastro.getText().toString();
        senha = edtSenhaCadastro.getText().toString();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    salvarDadosUsuario();
                    Snackbar snackbar = Snackbar.make(view, mensagens[1], Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();
                }else{
                    String erro;
                    try {
                        throw task.getException();
                    }
                    catch(FirebaseAuthWeakPasswordException e){
                        erro = "Digite uma senha com no mínimo 6 caracteres.";
                        Log.e("Erro", e.toString());
                    }
                    catch (FirebaseAuthUserCollisionException e){
                        erro = "Usuário já cadastrado.";
                        Log.e("Erro", e.toString());
                    }
                    catch(FirebaseAuthInvalidCredentialsException e){
                        erro = "E-mail inválido";
                        Log.e("Erro", e.toString());
                    }
                    catch (Exception e){
                        erro = "Erro ao cadastrar usuário.";
                        Log.e("Erro", e.toString());
                    }
                    Snackbar snackbar = Snackbar.make(view, erro, Snackbar.LENGTH_SHORT);
                    snackbar.setBackgroundTint(Color.WHITE);
                    snackbar.setTextColor(Color.BLACK);
                    snackbar.show();

                }

            }


        });
    }
    private void telaLogin(){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void salvarDadosUsuario() {
        nome = edtNomeCadastro.getText().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> usuarios = new HashMap<>();
        usuarios.put("nome", nome);

        //Pega o usuário autenticado no banco
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);

        documentReference.set(usuarios).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("db", "Sucesso ao salvar os dados!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("db", "Erro ao salvar dados!" + e.toString());
            }
        });

    }
}