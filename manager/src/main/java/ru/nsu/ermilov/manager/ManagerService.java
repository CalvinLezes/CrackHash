package ru.nsu.ermilov.manager;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_request.ObjectFactory;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;

import java.util.*;

@Service
public class ManagerService {
    private final WebClient webClient;

    private final ObjectFactory factory;

    private static final Integer PART_COUNT = 1;

    private static final Integer PART_NUMBER = 0;

    private static final int TIMEOUT = 100000;

    Map<String, CrackTask> tasks;

    ManagerService(){
        webClient = WebClient.builder()
                .baseUrl("http://worker:8081")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .build();
        tasks = new HashMap<>();
        factory = new ObjectFactory();
    }

    public StatusResponse getStatusById(String requestId){
        var task = tasks.get(requestId);
        if(task == null){
            return StatusResponse.builder()
                    .status(Status.ERROR)
                    .data(null)
                    .build();
        }
        if(task.getStatus().equals(Status.IN_PROGRESS)
                    && System.currentTimeMillis() - task.getCreated() >= TIMEOUT){
            task.setStatus(Status.ERROR);
        }
        return StatusResponse.builder()
                .status(task.getStatus())
                .data(task.getData())
                .build();
    }

    private void sendTask(UUID requestId, CrackRequest request){
        var managerRequest = createCrackHashManagerRequest(requestId, request);

        webClient.post()
                .uri("/internal/api/worker/hash/crack/task")
                .bodyValue(managerRequest)
                .retrieve()
                .toBodilessEntity()
                .subscribe();
    }

    public CrackResponse createTask(CrackRequest request) {
        UUID requestId = UUID.randomUUID();
        tasks.put(requestId.toString(),CrackTask.builder()
                .status(Status.IN_PROGRESS)
                .data(null)
                .created(System.currentTimeMillis())
                .build());
        sendTask(requestId, request);
        return CrackResponse.builder().requestId(requestId.toString()).build();
    }

    public void handleWorkerResponse(CrackHashWorkerResponse response){
        tasks.computeIfPresent(response.getRequestId(), (key, value) -> {
            value.setStatus(Status.READY);
            value.setData(response.getAnswers().getWords());
            return value;
        });
    }

    private CrackHashManagerRequest createCrackHashManagerRequest(UUID requestId, CrackRequest request){
        var alphabetString = "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,1,2,3,4,5,6,7,8,9,0";
        var alphabetList = new ArrayList<>(Arrays.asList(alphabetString.split(",")));
        var alphabet = factory.createCrackHashManagerRequestAlphabet();
        alphabet.getSymbols().addAll(alphabetList);
        var managerRequest = factory.createCrackHashManagerRequest();
        managerRequest.setRequestId(requestId.toString());
        managerRequest.setHash(request.getHash());
        managerRequest.setMaxLength(request.getMaxLength());
        managerRequest.setPartCount(PART_COUNT);
        managerRequest.setPartNumber(PART_NUMBER);
        managerRequest.setAlphabet(alphabet);
        return managerRequest;
    }


}
