import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.Map;

public class ExamAgent extends Agent {
    private final Map<String, Double> examGrades = new HashMap<>();

    @Override
    protected void setup() {
        examGrades.put("1001", 18.0);
        examGrades.put("1002", 14.5);
        examGrades.put("1003", 12.0);

        ServiceDescription sd = new ServiceDescription();
        sd.setType("grade.exam");
        sd.setName(getLocalName());
        registerService(sd);

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String studentId = msg.getContent();
                    Double grade = examGrades.get(studentId);
                    ACLMessage reply = msg.createReply();
                    if (grade != null) {
                        reply.setContent(String.valueOf(grade));
                    } else {
                        reply.setContent("NA");
                    }
                    send(reply);
                } else {
                    block();
                }
            }
        });
    }

    private void registerService(ServiceDescription sd) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}