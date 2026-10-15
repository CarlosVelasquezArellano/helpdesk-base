package com.nttdata.ms_tickets;

import com.nttdata.ms_tickets.api.TicketsApiDelegate;
import com.nttdata.ms_tickets.model.Ticket;
import com.nttdata.ms_tickets.model.TicketRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketApiDelegateImpl implements TicketsApiDelegate {

    private final List<Ticket> tickets = new ArrayList<>(List.of(
            ticket(1L, "No carga el reporte mensual", Ticket.PrioridadEnum.ALTA, Ticket.EstadoEnum.ABIERTO, 4),
            ticket(2L, "Error de ortografía en el menú", Ticket.PrioridadEnum.BAJA, Ticket.EstadoEnum.ABIERTO, 24),
            ticket(3L, "Lentitud al iniciar sesión", Ticket.PrioridadEnum.MEDIA, Ticket.EstadoEnum.CERRADO, 8)
    ));

    private long siguienteId = 4L;
    private final static int HORA_ALTA = 4;
    private final static int HORA_MEDIA = 8;
    private final static int HORA_BAJA = 24;

    @Override
    public ResponseEntity<Ticket> crearTicket(TicketRequest ticketRequest) {

        if (ticketRequest == null
                || ticketRequest.getTitulo() == null
                || ticketRequest.getTitulo().trim().length() < 5
                || ticketRequest.getTitulo().trim().length() > 100
                || ticketRequest.getPrioridad() == null) {
            return ResponseEntity.badRequest().build();
        }


        Ticket.PrioridadEnum prioridad =
                Ticket.PrioridadEnum.valueOf(ticketRequest.getPrioridad().name());

        int horasEstimadas = switch (prioridad) {
            case ALTA -> HORA_ALTA;
            case MEDIA -> HORA_MEDIA;
            case BAJA -> HORA_BAJA;
        };

        Ticket nuevoTicket = ticket(
                siguienteId++,
                ticketRequest.getTitulo(),
                prioridad,
                Ticket.EstadoEnum.ABIERTO,
                horasEstimadas
        );

        tickets.add(nuevoTicket);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTicket);
    }

    @Override
    public ResponseEntity<List<Ticket>> listarTickets(String estado) {

        if (estado == null || estado.isBlank()) {
            return ResponseEntity.ok(tickets);
        }

        List<Ticket> coinciden = tickets.stream()
                .filter(t -> t.getEstado().name().equalsIgnoreCase(estado))
                .toList();

        return ResponseEntity.ok(coinciden);
    }

    @Override
    public ResponseEntity<Ticket> obtenerTicket(Long id) {
        return tickets.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private static Ticket ticket(Long id, String titulo, Ticket.PrioridadEnum prioridad, Ticket.EstadoEnum estado, Integer horasEstimadas) {
        return new Ticket().id(id).titulo(titulo).prioridad(prioridad).estado(estado).horasEstimadas(horasEstimadas);
    }
}
