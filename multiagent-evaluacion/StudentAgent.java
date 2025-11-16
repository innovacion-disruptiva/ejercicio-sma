import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class StudentAgent extends Agent {
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            final String studentId = (String) args[0];
            System.out.println("Solicitud de calificación para el estudiante " + studentId);
            ServiceDescription service = new ServiceDescription();
            service.setType("grade.calculate");
            addBehaviour(new CyclicBehaviour(this) {
                private boolean requested = false;

                @Override
                public void action() {
                    try {
                        if (!requested) {
                            DFAgentDescription dfd = new DFAgentDescription();
                            dfd.addServices(service);
                            DFAgentDescription[] results = DFService.search(myAgent, dfd);
                            if (results.length > 0) {
                                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                                msg.addReceiver(results[0].getName());
                                msg.setContent(studentId);
                                send(msg);
                                requested = true;
                            }
                        }
                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }
                    ACLMessage response = receive();
                    if (response != null) {
                        System.out.println(getLocalName() + " recibió calificación final: " + response.getContent());
                        doDelete();
                    } else {
                        block();
                    }
                }
            });
        } else {
            System.err.println("Debes proporcionar el ID del estudiante como argumento");
            doDelete();
        }
    }
}