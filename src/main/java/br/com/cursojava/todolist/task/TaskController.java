package br.com.cursojava.todolist.task;

import br.com.cursojava.todolist.user.IUserRepository;
import br.com.cursojava.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/create")
    public ResponseEntity create(@RequestBody TaskModel taskReceived, HttpServletRequest request) {
        taskReceived.setIdUser((UUID) request.getAttribute("idUser"));

        // Checking if end date is after start date
        if(taskReceived.getEndAt().isBefore(taskReceived.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("End date must be after start date");
        }

        // Checking if start date is after current date
        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskReceived.getStartAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Start date must be after current date");
        }

        var taskCreated = this.taskRepository.save(taskReceived);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCreated);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity update(@PathVariable UUID id, @RequestBody TaskModel taskReceived, HttpServletRequest request) {

        // Getting task by id
        var task = this.taskRepository.findById(id).get();

        // Checking if task exists
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }

        // Getting idUser from request
        var idUser = (UUID) request.getAttribute("idUser");

        // Checking if user has permission to update task
        if(!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have permission to update this task");
        }

        // Setting new values
        Utils.copyNonNullProperties(taskReceived, task);

        // Saving task and returning it
        this.taskRepository.save(task);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/list")
    public ResponseEntity list(HttpServletRequest request) {
        var idUser = (UUID) request.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);
        return ResponseEntity.ok(tasks);
    }
}
