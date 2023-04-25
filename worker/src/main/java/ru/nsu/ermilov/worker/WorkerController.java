package ru.nsu.ermilov.worker;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;

@RestController
@RequestMapping("/internal/api/worker")
public class WorkerController {

    WorkerService workerService;

    WorkerController(WorkerService workerService){
        this.workerService = workerService;
    }

    @PostMapping(value="/hash/crack/task", consumes = MediaType.APPLICATION_XML_VALUE)
    public void getTask(@RequestBody CrackHashManagerRequest crackRequest) {
        workerService.doTask(crackRequest);
    }

}
