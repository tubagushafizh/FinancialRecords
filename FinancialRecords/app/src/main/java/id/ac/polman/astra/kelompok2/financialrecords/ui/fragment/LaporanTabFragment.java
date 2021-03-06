package id.ac.polman.astra.kelompok2.financialrecords.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import id.ac.polman.astra.kelompok2.financialrecords.R;
import id.ac.polman.astra.kelompok2.financialrecords.adapter.LaporanAdapter;
import id.ac.polman.astra.kelompok2.financialrecords.adapter.RecyclerAdapter;
import id.ac.polman.astra.kelompok2.financialrecords.model.DashboardModel;
import id.ac.polman.astra.kelompok2.financialrecords.model.KategoriModel;
import id.ac.polman.astra.kelompok2.financialrecords.model.LaporanModel;
import id.ac.polman.astra.kelompok2.financialrecords.ui.activity.HomeActivity;
import id.ac.polman.astra.kelompok2.financialrecords.utils.Validation;

public class LaporanTabFragment extends Fragment {
    private View objectKTF;

    List<LaporanModel> listLaporan = new ArrayList<>();
    RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db;
    LaporanAdapter mLaporanAdapter;
    LaporanModel laporanModel = new LaporanModel();
    Validation valid;

    RecyclerView rv_view_laporan;
    CardView cv_kategori_laporan;
    TextView total_pemasukan;
    TextView total_pengeluaran;
    TextView selisih;

    EditText input_minimal;
    Button btn_minimal;
    EditText input_maximal;
    Button btn_maximal;
    Button btn_cari;

    Calendar calendar = Calendar.getInstance();
    Locale id = new Locale("in","ID");
    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd-MM-YYYY", id);
    DashboardModel dashboardModel = new DashboardModel();

    Date date_minimal;
    Date date_maximal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        objectKTF = inflater.inflate(R.layout.fragment_laporan_list, container, false);

        rv_view_laporan = objectKTF.findViewById(R.id.rv_view_laporan);
        cv_kategori_laporan = objectKTF.findViewById(R.id.cv_kategori_laporan);

        total_pemasukan = objectKTF.findViewById(R.id.total_pemasukan);
        total_pengeluaran = objectKTF.findViewById(R.id.total_pengeluaran);
        selisih = objectKTF.findViewById(R.id.selisih);

        input_minimal = objectKTF.findViewById(R.id.input_minimal);
        input_maximal = objectKTF.findViewById(R.id.input_maximal);
        btn_minimal = objectKTF.findViewById(R.id.btn_minimal);
        btn_maximal = objectKTF.findViewById(R.id.btn_maximal);
        btn_cari = objectKTF.findViewById(R.id.cari);

        db = FirebaseFirestore.getInstance();

        showTotalData();

        rv_view_laporan.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        rv_view_laporan.setLayoutManager(layoutManager);
        //rv_view.setItemAnimator(new DefaultItemAnimator());

        showData();

        btn_minimal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        input_minimal.setText(mSimpleDateFormat.format(calendar.getTime()));
                        //supaya bisa pas date today
                        calendar.add(Calendar.DATE, -1);
                        date_minimal = calendar.getTime();

//                        private Date yesterday() {
//                            final Calendar cal = Calendar.getInstance();
//                            cal.add(Calendar.DATE, -1);
//                            return cal.getTime();
//                        }

                        String input1 = input_minimal.getText().toString();
                        String input2 = input_maximal.getText().toString();

                        if(input1.isEmpty()&&input2.isEmpty()){
                            btn_cari.setEnabled(false);
                        }else{
                            btn_cari.setEnabled(true);
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btn_maximal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        input_maximal.setText(mSimpleDateFormat.format(calendar.getTime()));
                        date_maximal = calendar.getTime();

                        String input1 = input_minimal.getText().toString();
                        String input2 = input_maximal.getText().toString();

                        if(input1.isEmpty()&&input2.isEmpty()){
                            btn_cari.setEnabled(false);
                        }else{
                            btn_cari.setEnabled(true);
                        }
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        btn_cari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateData();
            }
        });

        return objectKTF;
    }

    private void showDateData() {
        Log.e("BUTTON CARI", " MASUK DATE DATA");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String email = firebaseUser.getEmail();
        db.collection("user").document(email)
            .collection("Laporan")
            .orderBy("tanggal", Query.Direction.ASCENDING)
            .startAt(date_minimal).endAt(date_maximal)
            //.startAfter(date_minimal.getTime()).endBefore(date_maximal.getTime())
            .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //showLisener(task);
                        if (task.isSuccessful()) {
                            Log.e("BUTTON CARI", "isSuccessfull :" +date_minimal +"after" +date_maximal);
                            listLaporan.clear();

                            //show data
                            int listjumlahtam = 0;
                            int listjumtam = 0;
                            int listjumlahkur = 0;
                            int listjumkur = 0;
                            int listkategori = 0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LaporanModel laporanModel = new LaporanModel();
                                laporanModel.setJenis_kategori(document.getLong("jenis_kategori").intValue());
                                laporanModel.setJumlah((document.getLong("jumlah").intValue()));
                                laporanModel.setKeterangan(document.getString("keterangan"));
                                laporanModel.setKategori(document.getString("kategori"));
                                laporanModel.setTanggal(document.getDate("tanggal"));
                                listLaporan.add(laporanModel);

                                Log.e("BUTTON CARI", " selesai FOR");

                                //getTotalPemasukan
                                listkategori = (int) document.getLong("jenis_kategori").intValue();

                                if(listkategori == 1){
                                    listjumlahtam = (int) document.getLong("jumlah").intValue();
                                    Log.e("LAPORAN TAMBAH", "listjumlah :" + listjumlahtam);

                                    listjumtam = listjumtam + listjumlahtam;
                                    Log.e("LAPORAN LIST TAMBAH", "listjum :" + listjumtam);
                                }else if (listkategori == 2){
                                    listjumlahkur = (int) document.getLong("jumlah").intValue();
                                    Log.e("LAPORAN KURANG", "listjumlah :" + listjumlahkur);

                                    listjumkur = listjumkur + listjumlahkur;
                                    Log.e("LAPORAN LIST KURANG", "listjum :" + listjumkur);
                                }

                            }
                            laporanModel.setTotal_pemasukan(listjumtam);
                            String totalpem = String.valueOf(laporanModel.getTotal_pemasukan());
                            Log.e("LAPORAN", "Total Pemasukan :" + totalpem);

                            total_pemasukan = objectKTF.findViewById(R.id.total_pemasukan);
                            total_pemasukan.setText(formatRupiah(Double.parseDouble(totalpem)));

                            laporanModel.setTotal_pengeluaran(listjumkur);
                            String totalpen = String.valueOf(laporanModel.getTotal_pengeluaran());
                            Log.e("LAPORAN", "Total Pengeluaran :" + totalpen);
                            Log.e("LAPORAN", "Total Pemasukan di pengeluaran :" + String.valueOf(laporanModel.getTotal_pemasukan()));

                            total_pengeluaran = objectKTF.findViewById(R.id.total_pengeluaran);
                            total_pengeluaran.setText(formatRupiah(Double.parseDouble(totalpen)));

                            //SELISIH
                            int totpem = laporanModel.getTotal_pemasukan();
                            int totpen = laporanModel.getTotal_pengeluaran();
                            int totsel = totpem - totpen;
                            Log.e("SELISIH", "Selisih :" + String.valueOf(totsel));
                            selisih = objectKTF.findViewById(R.id.selisih);
                            selisih.setText(formatRupiah(Double.parseDouble(String.valueOf(totsel))));
                        }
                        mLaporanAdapter = new LaporanAdapter(LaporanTabFragment.this, listLaporan);
                        rv_view_laporan.setAdapter(mLaporanAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", e.getMessage());
                    }
                });
    }

    private void showData() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String email = firebaseUser.getEmail();

        db.collection("user").document(email)
                .collection("Laporan")
                .orderBy("tanggal", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                LaporanModel laporanModel = new LaporanModel();
                                laporanModel.setJenis_kategori(document.getLong("jenis_kategori").intValue());
                                laporanModel.setJumlah((document.getLong("jumlah").intValue()));
                                laporanModel.setKategori(document.getString("kategori"));
                                laporanModel.setKeterangan(document.getString("keterangan"));
                                laporanModel.setTanggal(document.getDate("tanggal"));
                                listLaporan.add(laporanModel);
                            }
                        }
                        mLaporanAdapter = new LaporanAdapter(LaporanTabFragment.this, listLaporan);
                        rv_view_laporan.setAdapter(mLaporanAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", e.getMessage());
                    }
                });
    }

    private void showTotalData(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        //getTotalPemasukan
        db.collection("user").document(firebaseUser.getEmail())
            .collection("Laporan").whereEqualTo("jenis_kategori", 1)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    //show data
                    int listjumlah = 0;
                    int listjum = 0;

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        listjumlah = (int) doc.getLong("jumlah").intValue();
                        Log.e("LAPORAN", "listjumlah :" + listjumlah);

                        listjum = listjum + listjumlah;
                        Log.e("LAPORAN LIST", "listjum :" + listjum);
                    }

                    laporanModel.setTotal_pemasukan(listjum);
                    String totalpem = String.valueOf(laporanModel.getTotal_pemasukan());
                    Log.e("LAPORAN", "Total Pemasukan :" + totalpem);

                    total_pemasukan = objectKTF.findViewById(R.id.total_pemasukan);
                    total_pemasukan.setText(formatRupiah(Double.parseDouble(totalpem)));
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("TAG", e.getMessage());
                }
            });

        //getTotalPengeluaran
        db.collection("user").document(firebaseUser.getEmail())
            .collection("Laporan").whereEqualTo("jenis_kategori", 2)
            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //show data
                        int listjumlah = 0;
                        int listjum = 0;

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            listjumlah = (int) doc.getLong("jumlah").intValue();
                            Log.e("LAPORAN", "listjumlah :" + listjumlah);

                            listjum = listjum + listjumlah;
                            Log.e("LAPORAN LIST", "listjum :" + listjum);
                        }

                        laporanModel.setTotal_pengeluaran(listjum);
                        String totalpen = String.valueOf(laporanModel.getTotal_pengeluaran());

                        total_pengeluaran = objectKTF.findViewById(R.id.total_pengeluaran);
                        total_pengeluaran.setText(formatRupiah(Double.parseDouble(totalpen)));

                        db.collection("user").document(firebaseUser.getEmail()).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()){
                                            DocumentSnapshot doc = task.getResult();
                                            int saldo = doc.getLong("saldo").intValue();
                                            dashboardModel.setSaldo(saldo);

                                            selisih = objectKTF.findViewById(R.id.selisih);
                                            selisih.setText(formatRupiah(Double.parseDouble(Integer.toString(dashboardModel.getSaldo()))));
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("TAG", e.getMessage());
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", e.getMessage());
                    }
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String formatRupiah(Double number){
        Locale localeID = new Locale("in", "ID");
        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
        return formatRupiah.format(number);
    }

    private void showLisener(DataSnapshot snapshot) {
        listLaporan.clear();
        for (DataSnapshot item : snapshot.getChildren()) {
            //LaporanModel user = item.getDocumentReference(LaporanModel.class);
            LaporanModel laporanModel = item.getValue(LaporanModel.class);
            listLaporan.add(laporanModel);
        }
        mLaporanAdapter = new LaporanAdapter(LaporanTabFragment.this, listLaporan);
        rv_view_laporan.setAdapter(mLaporanAdapter);
    }
}
