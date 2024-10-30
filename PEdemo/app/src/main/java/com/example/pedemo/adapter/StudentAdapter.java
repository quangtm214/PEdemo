package com.example.pedemo.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pedemo.databinding.ItemStudentBinding;
import com.example.pedemo.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> studentList = new ArrayList<>();
    private OnStudentClickListener listener;
    private OnStudentLongClickListener longClickListener;

    public interface OnStudentClickListener {
        void onStudentClick(Student student);
    }

    public void setOnStudentClickListener(OnStudentClickListener listener) {
        this.listener = listener;
    }
    public void setOnStudentLongClickListener(OnStudentLongClickListener listener) {
        this.longClickListener = listener;
    }

    public interface OnStudentLongClickListener {
        boolean onStudentLongClick(Student student); // Đơn giản hóa interface
    }


    public StudentAdapter(List<Student> studentList) {
        if (studentList != null) {
            this.studentList = studentList;
        }
    }

    public void updateStudents(List<Student> students) {
        this.studentList = students != null ? students : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemStudentBinding binding = ItemStudentBinding.inflate(inflater, parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public class StudentViewHolder extends RecyclerView.ViewHolder {
        private final ItemStudentBinding binding;

        public StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onStudentClick(studentList.get(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    return longClickListener.onStudentLongClick(studentList.get(position));
                }
                return false;
            });
        }

        public void bind(Student student) {
            binding.StudentName.setText(student.getName());
            binding.StudentEmail.setText(student.getEmail());
            binding.StudentDate.setText(student.getDate());
            binding.StudentGender.setText(student.getGender());
            binding.StudentAddress.setText(student.getAddress());
            String majorName = "Unknown Major";
            if (student.getMajor() != null && student.getMajor().getNameMajor() != null) {
                majorName = student.getMajor().getNameMajor();
            }
            binding.StudentMajor.setText(majorName);
        }
    }
}
