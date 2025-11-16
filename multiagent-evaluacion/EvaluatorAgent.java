import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class EvaluatorAgent extends Agent {
    @Override
    protected void setup() {
        ServiceDescription sd = new ServiceDescription();
        sd.setType("grade.calculate");
        sd.setName(getLocalName());
        registerService(sd);

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String studentId = msg.getContent();
                    ACLMessage reply = msg.createReply();
                    Double examGrade = requestGrade(studentId, "grade.exam");
                    Double assignmentGrade = requestGrade(studentId, "grade.assignment");
                    if (examGrade != null && assignmentGrade != null) {
                        double finalGrade = examGrade * 0.6 + assignmentGrade * 0.4;
                        reply.setContent(String.valueOf(finalGrade));
                    } else {
                        reply.setContent("No grade data");
                    }
                    send(reply);
                } else {
                    block();
                }
            }
        });
    }

    private Double requestGrade(String studentId, String serviceType) {
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd);
        try {
            DFAgentDescription[] results = DFService.search(this, dfd);
            if (results.length > 0) {
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.addReceiver(results[0].getName());
                request.setContent(studentId);
                send(request);
                ACLMessage response = blockingReceive();
                if (response != null) {
                    try {
                        return Double.parseDouble(response.getContent());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
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