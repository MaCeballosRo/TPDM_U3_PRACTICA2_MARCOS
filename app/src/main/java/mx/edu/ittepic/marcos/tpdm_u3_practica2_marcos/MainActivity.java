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

public class MainActivity extends AppCompatActivity {
    EditText nombre,noControl,edad,carrera;
    Button insertar,eliminar;
    List<Alumno> datosAlumnos;
    List<String> ramas;
    ListView lista;

    FirebaseFirestore servicioBaseDatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nombre = findViewById(R.id.nombreA);
        noControl = findViewById(R.id.noControl);
        edad = findViewById(R.id.edad);
        carrera = findViewById(R.id.carrera);
        insertar = findViewById(R.id.insertarA);
        eliminar = findViewById(R.id.eliminarA);
        lista = findViewById(R.id.lista);

        servicioBaseDatos = FirebaseFirestore.getInstance();
        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarAlumnos();
            }
        });
        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eliminarA();
            }
        });


    }

    protected void onStart(){
        consultarTodos();
        super.onStart();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.opciones,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.consultarCarrera:
                Intent consultarP = new Intent(this,Main2Activity.class);
                startActivity(consultarP);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void insertarAlumnos(){
        String A_NC = noControl.getText().toString();
        String A_nombre = nombre.getText().toString();
        String A_carrera = carrera.getText().toString();
        String A_edad = edad.getText().toString();

        if(A_NC.equals("")){
            Toast.makeText(MainActivity.this, "Debe establecer un número de control",Toast.LENGTH_SHORT).show();
            return;
        }

        Alumno alu = new Alumno(A_NC,A_nombre,A_carrera,A_edad);

        servicioBaseDatos.collection("alumnos")
                .document(noControl.getText().toString())
                .set(alu)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Se insertó correctamente",Toast.LENGTH_SHORT).show();
                        noControl.setText("");
                        nombre.setText("");
                        carrera.setText("");
                        edad.setText("");
                        consultarTodos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error! No se pudo insertar",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void consultarTodos(){
        servicioBaseDatos.collection("alumnos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        datosAlumnos = new ArrayList<>();
                        ramas = new ArrayList<>();
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot registro: task.getResult()){
                                Map<String, Object> datos = registro.getData();

                                ramas.add(registro.getId()); // Se guarda el ID de este registro
                                Alumno al = new Alumno(datos.get("noControl").toString(), datos.get("nombreA").toString(),datos.get("carrera").toString(),datos.get("edad").toString());

                                datosAlumnos.add(al); // Agregando al alumno

                            }
                            ponerloEnListView();
                        }else{
                            Toast.makeText(MainActivity.this, "Error! No se pudo mostrar datos",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void ponerloEnListView(){
        if(datosAlumnos.size()==0){
            return;
        }

        String[] datos = new String[datosAlumnos.size()];

        for(int i=0; i<datos.length; i++){
            Alumno al = datosAlumnos.get(i);
            datos[i] = al.noControl+"--"+al.nombreA;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datos);

        lista.setAdapter(adapter);

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
                final View alumnoD = getLayoutInflater().inflate(R.layout.alumnos,null);
                Alumno a = datosAlumnos.get(position);
                TextView A_NC = alumnoD.findViewById(R.id.NC);
                TextView A_Nombre = alumnoD.findViewById(R.id.nombreAlumno);
                TextView A_Edad = alumnoD.findViewById(R.id.edadAlumno);
                TextView A_Carrera = alumnoD.findViewById(R.id.carreraAlumno);

                A_NC.setText(a.noControl);
                A_Nombre.setText(a.nombreA);
                A_Edad.setText(a.edad);
                A_Carrera.setText(a.carrera);

                alerta.setTitle("INFORMACION").setMessage("Datos de alumno")
                        .setView(alumnoD)
                        .setPositiveButton("OK",null)
                        .show();
            }
        });
    }

    private void eliminarA(){
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
                            Toast.makeText(MainActivity.this, "El ID está vacío",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        eliminarAlumno2(idEliminar.getText().toString());
                    }
                })
                .setNegativeButton("Cancelar",null)
                .show();
    }

    private void eliminarAlumno2(String idEliminar){
        servicioBaseDatos.collection("alumnos")
                .document(idEliminar)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Se eliminó correctamente",Toast.LENGTH_SHORT).show();
                        consultarTodos();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error! No se pudo eliminar",Toast.LENGTH_SHORT).show();
                    }
                });

    }

}
