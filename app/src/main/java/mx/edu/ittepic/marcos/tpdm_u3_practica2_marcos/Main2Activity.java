package mx.edu.ittepic.marcos.tpdm_u3_practica2_marcos;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {
    ListView listaC;
    List<Carrera> datosCarrera;
    List<String> ramasC;
    FirebaseFirestore servicioBaseDatos;
    EditText nombreC, idC,semestres;
    Button insertar, eliminar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        listaC = findViewById(R.id.listaCarreras);
        nombreC = findViewById(R.id.nombreC);
        idC = findViewById(R.id.idCarrera);
        semestres = findViewById(R.id.nSemestres);
        insertar = findViewById(R.id.insertarC);
        eliminar = findViewById(R.id.eliminarC);
        servicioBaseDatos = FirebaseFirestore.getInstance();

        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarCarrera();
            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarCarrera();
            }
        });
    }

    protected void onStart(){
        consultarTodos();
        super.onStart();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.opciones2,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.adminAlumnos:
                Intent consultarP = new Intent(this,MainActivity.class);
                startActivity(consultarP);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void insertarCarrera(){
        String C_ID = idC.getText().toString();
        String C_nombre = nombreC.getText().toString();
        String C_semestres = semestres.getText().toString();

        if(C_ID.equals("")){
            Toast.makeText(Main2Activity.this, "Debe establecer un identificador de la carrera",Toast.LENGTH_SHORT).show();
            return;
        }

        Carrera alu = new Carrera(C_ID,C_nombre,C_semestres);

        servicioBaseDatos.collection("carreras")
                .document(C_ID)
                .set(alu)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Main2Activity.this, "Se insertó correctamente",Toast.LENGTH_SHORT).show();
                        idC.setText("");
                        nombreC.setText("");
                        semestres.setText("");;
                        consultarTodos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Main2Activity.this, "Error! No se pudo insertar",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void eliminarCarrera(){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        final EditText idEliminar = new EditText(this);
        idEliminar.setHint("No debe quedar vacío");

        alerta.setTitle("ATENCION")
                .setMessage("Id a eliminar:")
                .setView(idEliminar)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(idEliminar.getText().toString().isEmpty()){
                            Toast.makeText(Main2Activity.this, "El ID está vacío",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        eliminarAlumno2(idEliminar.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar",null)
                .show();
    }

    private void eliminarAlumno2(String idEliminar){
        servicioBaseDatos.collection("carreras")
                .document(idEliminar)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Main2Activity.this, "Se eliminó correctamente",Toast.LENGTH_SHORT).show();
                        consultarTodos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Main2Activity.this, "Error! No se pudo eliminar",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void consultarTodos(){
        servicioBaseDatos.collection("carreras")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        datosCarrera = new ArrayList<>();
                        ramasC = new ArrayList<>();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot registro: task.getResult()){
                                Map<String, Object> datos = registro.getData();

                                ramasC.add(registro.getId()); // Se guarda el ID de este registro
                                Carrera c = new Carrera(datos.get("idCarrera").toString(), datos.get("nombreC").toString(),datos.get("noSemestres").toString());

                                datosCarrera.add(c); // Agregando al alumno

                            }
                            ponerloEnListView();
                        }else{
                            Toast.makeText(Main2Activity.this, "Error! No se pudo mostrar datos",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void ponerloEnListView(){
        if(datosCarrera.size()==0){
            return;
        }

        String[] datos = new String[datosCarrera.size()];

        for(int i=0; i<datos.length; i++){
            Carrera al = datosCarrera.get(i);
            datos[i] = al.idCarrera+"\n"+al.nombreC;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datos);

        listaC.setAdapter(adapter);

        listaC.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(Main2Activity.this);
                final View carreraD = getLayoutInflater().inflate(R.layout.carreras,null);
                Carrera c = datosCarrera.get(position);
                TextView ID_C1 = carreraD.findViewById(R.id.ID);
                TextView ID_Nombre1 = carreraD.findViewById(R.id.nombreCarrera);
                TextView ID_Semestres1 = carreraD.findViewById(R.id.semestres);

                ID_C1.setText(c.idCarrera);
                ID_Nombre1.setText(c.nombreC);
                ID_Semestres1.setText(c.noSemestres);

                alerta.setTitle("INFORMACION").setMessage("Datos de carrera")
                        .setView(carreraD)
                        .setPositiveButton("OK",null)
                        .show();
            }
        });
    }




}
