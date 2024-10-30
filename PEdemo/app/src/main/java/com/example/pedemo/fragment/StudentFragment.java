package com.example.pedemo.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pedemo.adapter.StudentAdapter;
import com.example.pedemo.databinding.DialogAddStudentBinding;
import com.example.pedemo.databinding.FragmentStudentBinding;
import com.example.pedemo.model.Major;
import com.example.pedemo.model.Student;
import com.example.pedemo.viewmodel.StudentViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StudentFragment extends Fragment {
    private FragmentStudentBinding binding;
    private StudentViewModel studentViewModel;
    private StudentAdapter studentAdapter;
    private List<Major> majorList;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize data binding
        binding = FragmentStudentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView
        binding.recyclerViewStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        studentAdapter = new StudentAdapter(new ArrayList<>());
        binding.recyclerViewStudents.setAdapter(studentAdapter);

        // Initialize ViewModel
        studentViewModel = new ViewModelProvider(this).get(StudentViewModel.class);

        // Observe students data
        studentViewModel.getStudentsLiveData().observe(getViewLifecycleOwner(), students -> {
            studentAdapter.updateStudents(students);
        });

        // Observe majors data for spinner
        studentViewModel.getMajorsLiveData().observe(getViewLifecycleOwner(), majors -> {
            majorList = majors;
        });

        // Setup FAB click listener
        binding.fabAddStudent.setOnClickListener(v -> showAddStudentDialog());

        // Fetch initial data
        studentViewModel.fetchMajors();
        studentViewModel.fetchStudents();

        studentAdapter.setOnStudentClickListener(this::showUpdateStudentDialog);
        studentAdapter.setOnStudentLongClickListener(student -> {
            showDeleteConfirmDialog(student);
            return true;
        });

        return binding.getRoot();
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Sử dụng binding cho dialog
        DialogAddStudentBinding dialogBinding = DialogAddStudentBinding.inflate(getLayoutInflater());

        // Setup date picker
        dialogBinding.editTextDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                        dialogBinding.editTextDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Setup major spinner
        if (majorList != null && !majorList.isEmpty()) {
            // Tạo adapter với layout mặc định cho spinner
            ArrayAdapter<Major> majorAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    majorList
            );

            // Set layout cho dropdown menu
            majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set adapter cho spinner
            dialogBinding.spinnerMajor.setAdapter(majorAdapter);
        } else {
        }

        builder.setView(dialogBinding.getRoot())
                .setTitle("Add Student")
                .setPositiveButton("Add", (dialog, which) -> {
                    if (validateInput(dialogBinding)) {
                        Student student = new Student();
                        student.setName(dialogBinding.editTextName.getText().toString().trim());
                        student.setDate(dialogBinding.editTextDate.getText().toString().trim());
                        student.setGender(dialogBinding.radioMale.isChecked() ? "Male" : "Female");
                        student.setEmail(dialogBinding.editTextEmail.getText().toString().trim());
                        student.setAddress(dialogBinding.editTextAddress.getText().toString().trim());

                        Major selectedMajor = (Major) dialogBinding.spinnerMajor.getSelectedItem();
                        student.setMajorId(selectedMajor.getIDMajor());

                        studentViewModel.addStudent(student);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showUpdateStudentDialog(Student student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        DialogAddStudentBinding dialogBinding = DialogAddStudentBinding.inflate(getLayoutInflater());

        // Pre-fill existing data
        dialogBinding.editTextName.setText(student.getName());
        dialogBinding.editTextDate.setText(student.getDate());
        dialogBinding.editTextEmail.setText(student.getEmail());
        dialogBinding.editTextAddress.setText(student.getAddress());
        if ("Nam".equals(student.getGender())) {
            dialogBinding.radioMale.setChecked(true);
        } else {
            dialogBinding.radioFemale.setChecked(true);
        }

        // Setup date picker
        dialogBinding.editTextDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                        dialogBinding.editTextDate.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Setup major spinner
        if (majorList != null && !majorList.isEmpty()) {
            ArrayAdapter<Major> majorAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    majorList
            );
            majorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dialogBinding.spinnerMajor.setAdapter(majorAdapter);

            // Set selected major
            for (int i = 0; i < majorList.size(); i++) {
                if (majorList.get(i).getIDMajor().equals(student.getMajorId())) {
                    dialogBinding.spinnerMajor.setSelection(i);
                    break;
                }
            }
        }

        builder.setView(dialogBinding.getRoot())
                .setTitle("Update Student")
                .setPositiveButton("Update", (dialog, which) -> {
                    if (validateInput(dialogBinding)) {
                        // Update student object
                        student.setName(dialogBinding.editTextName.getText().toString().trim());
                        student.setDate(dialogBinding.editTextDate.getText().toString().trim());
                        student.setGender(dialogBinding.radioMale.isChecked() ? "Male" : "Female");
                        student.setEmail(dialogBinding.editTextEmail.getText().toString().trim());
                        student.setAddress(dialogBinding.editTextAddress.getText().toString().trim());

                        Major selectedMajor = (Major) dialogBinding.spinnerMajor.getSelectedItem();
                        student.setMajorId(selectedMajor.getIDMajor());

                        // Show loading
                        ProgressDialog progressDialog = new ProgressDialog(requireContext());
                        progressDialog.setMessage("Updating...");
                        progressDialog.show();

                        // Call update
                        studentViewModel.updateStudent(student);

                        // Dismiss loading and show success message
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(requireContext(), "Updated success", Toast.LENGTH_SHORT).show();
                        }, 1000);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmDialog(Student student) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm delete Student ")
                .setMessage("Do you want to delete this student?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Show loading
                    ProgressDialog progressDialog = new ProgressDialog(requireContext());
                    progressDialog.setMessage("Deleting...");
                    progressDialog.show();

                    // Delete student
                    studentViewModel.deleteStudent(student.getMajorId(),Integer.toString(student.getID()));

                    // Dismiss loading and show success message
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Deleted Success", Toast.LENGTH_SHORT).show();
                    }, 1000);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean validateInput(DialogAddStudentBinding binding) {
        boolean isValid = true;

        if (binding.editTextName.getText().toString().trim().isEmpty()) {
            binding.editTextName.setError("Please enter name");
            isValid = false;
        }

        if (binding.editTextDate.getText().toString().trim().isEmpty()) {
            binding.editTextDate.setError("Please enter date of birth");
            isValid = false;
        }

        if (binding.editTextEmail.getText().toString().trim().isEmpty()) {
            binding.editTextEmail.setError("Please enter email");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.editTextEmail.getText().toString()).matches()) {
            binding.editTextEmail.setError("Email unaviable");
            isValid = false;
        }

        if (binding.editTextAddress.getText().toString().trim().isEmpty()) {
            binding.editTextAddress.setError("Please enter Address");
            isValid = false;
        }

        if (binding.spinnerMajor.getSelectedItem() == null) {
            Toast.makeText(requireContext(), "Select Major", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
