package ru.nsu.ermilov.worker;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.nsu.ccfit.schema.crack_hash_response.ObjectFactory;

import java.util.ArrayList;
import java.util.List;

import static org.paukov.combinatorics.CombinatoricsFactory.createPermutationWithRepetitionGenerator;
import static org.paukov.combinatorics.CombinatoricsFactory.createVector;

@Service
public class WorkerService {

    WebClient webClient;
    ObjectFactory factory;

    WorkerService(){
        webClient = WebClient.builder()
                .baseUrl("http://manager:8080")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .build();
        factory = new ObjectFactory();
    }

    public void doTask(CrackHashManagerRequest request){
        List<String> words = new ArrayList<>();
        var vector = createVector(request.getAlphabet().getSymbols());
        var partCount = request.getPartCount();
        var partNumber = request.getPartNumber();
        var hash = request.getHash();
        for(int length = 1; length <= request.getMaxLength(); length++){
            var generator = createPermutationWithRepetitionGenerator(vector,length);
            var numberOfWords = (int) Math.pow(request.getAlphabet().getSymbols().size(), length);
            var startIndex = (long) Math.ceil((double) numberOfWords / partCount * partNumber);
            var stopIndex = (long) Math.ceil((double) numberOfWords / partCount * (partNumber + 1));
            var generatedWords = generator.generateObjectsRange(startIndex,stopIndex);
            words.addAll(
                    generatedWords
                            .stream()
                            .map(word -> String.join("", word))
                            .filter(word ->
                                    hash.equals(DigestUtils.md5DigestAsHex(word.getBytes()))
                    ).toList()
            );
        }
        sendResponse(createCrackHashWorkerResponse(request.getRequestId(), partNumber, words));
    }

    private CrackHashWorkerResponse createCrackHashWorkerResponse(String requestId, Integer partNumber, List<String> words){
        var answers = factory.createCrackHashWorkerResponseAnswers();
        answers.getWords().addAll(words);
        var crackHashWorkerResponse = factory.createCrackHashWorkerResponse();
        crackHashWorkerResponse.setRequestId(requestId);
        crackHashWorkerResponse.setPartNumber(partNumber);
        crackHashWorkerResponse.setAnswers(answers);
        return crackHashWorkerResponse;
    }

    private void sendResponse(CrackHashWorkerResponse response){
        webClient.patch()
                .uri("internal/api/manager/hash/crack/request")
                .bodyValue(response)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
