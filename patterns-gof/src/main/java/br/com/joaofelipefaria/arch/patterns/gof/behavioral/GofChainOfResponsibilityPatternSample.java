package br.com.joaofelipefaria.arch.patterns.gof.behavioral;

/**
 * <b>Chain of Responsibility</b> (Behavioral).
 * <p>
 * <b>Intent:</b> avoid coupling the sender of a request to its receiver by
 * giving more than one object a chance to handle the request. Chain the
 * receiving objects and pass the request along the chain until an object
 * handles it.
 * <p>
 * <b>Participants:</b>
 * <ul>
 *     <li>{@code Handler} ({@link DiscountApprover}) - defines an interface
 *         for handling requests and (optionally) for accessing the next
 *         handler in the chain.</li>
 *     <li>{@code ConcreteHandler} ({@link TeamLeadApprover}, {@link ManagerApprover},
 *         {@link DirectorApprover}) - handles requests it is responsible
 *         for; otherwise forwards the request to its successor.</li>
 *     <li>{@code Client} ({@link #main}) - initiates the request on the
 *         first handler in the chain.</li>
 * </ul>
 * <p>
 * <b>Use it when</b> more than one object may handle a request and the
 * handler isn't known ahead of time - here, a discount request is escalated
 * up an approval chain until someone with enough authority approves it,
 * without the requester needing to know the org chart.
 */
public class GofChainOfResponsibilityPatternSample {

    /** Handler: declares how to process a request and how to chain to the next handler. */
    abstract static class DiscountApprover {
        private DiscountApprover next;

        DiscountApprover setNext(DiscountApprover next) {
            this.next = next;
            return next;
        }

        final void approve(double discountPercentage) {
            if (canApprove(discountPercentage)) {
                System.out.println(getClass().getSimpleName() + " approved a " + discountPercentage + "% discount");
            } else if (next != null) {
                System.out.println(getClass().getSimpleName() + " cannot approve " + discountPercentage
                        + "%, forwarding to " + next.getClass().getSimpleName());
                next.approve(discountPercentage);
            } else {
                System.out.println("No approver in the chain could approve " + discountPercentage + "%");
            }
        }

        abstract boolean canApprove(double discountPercentage);
    }

    /** ConcreteHandler: lowest authority level. */
    static class TeamLeadApprover extends DiscountApprover {
        @Override
        boolean canApprove(double discountPercentage) {
            return discountPercentage <= 10;
        }
    }

    /** ConcreteHandler: mid authority level. */
    static class ManagerApprover extends DiscountApprover {
        @Override
        boolean canApprove(double discountPercentage) {
            return discountPercentage <= 25;
        }
    }

    /** ConcreteHandler: highest authority level. */
    static class DirectorApprover extends DiscountApprover {
        @Override
        boolean canApprove(double discountPercentage) {
            return discountPercentage <= 50;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Chain of Responsibility Pattern ===");

        DiscountApprover teamLead = new TeamLeadApprover();
        teamLead.setNext(new ManagerApprover()).setNext(new DirectorApprover());

        teamLead.approve(5);
        teamLead.approve(20);
        teamLead.approve(45);
        teamLead.approve(80);
    }
}
