package ru.nsu.ermilov.manager;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;

@RestController
@RequiredArgsConstructor
public class ManagerController {
    private final  ManagerService managerService;

    @PostMapping("/api/hash/crack")
    public ResponseEntity<?> Crack(@RequestBody CrackRequest request){
        var response = managerService.createTask(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/hash/status")
    public ResponseEntity<?> status(@RequestParam(name = "requestId") String requestId){
        var response =  managerService.getStatusById(requestId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/internal/api/manager/hash/crack/request")
    public void workerResponse(@RequestBody CrackHashWorkerResponse response){
        managerService.handleWorkerResponse(response);
    }
}
