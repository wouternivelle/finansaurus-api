package io.nivelle.finansaurus.payees.application;

import io.nivelle.finansaurus.payees.domain.Payee;
import io.nivelle.finansaurus.payees.domain.PayeeNotFoundException;
import io.nivelle.finansaurus.payees.domain.PayeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PayeeServiceImplTest {
    private PayeeService service;

    @Mock
    private PayeeRepository repository;

    @BeforeEach
    public void setup() {
        service = new PayeeServiceImpl(repository);
    }

    @Test
    public void whenSaving_thenSaved() {
        Payee payee = Payee.builder().build();
        when(repository.save(eq(payee)))
                .thenReturn(payee);

        Payee result = service.save(payee);

        verify(repository).save(eq(payee));
        assertThat(result, equalTo(payee));
    }

    @Test
    public void whenFetching_thenFetched() {
        Payee payee = Payee.builder().build();
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.of(payee));

        Payee result = service.fetch(1L);

        verify(repository).findById(eq(1L));
        assertThat(result, equalTo(payee));
    }

    @Test
    public void whenFetching_withNonExistingPayee_thenExceptionThrown() {
        when(repository.findById(eq(1L)))
                .thenReturn(Optional.empty());

        assertThrows(PayeeNotFoundException.class, () -> service.fetch(1L));
    }

    @Test
    public void whenDeleting_thenDeleted() {
        service.delete(1L);

        verify(repository).deleteById(eq(1L));
    }

    @Test
    public void whenDeleting_WithNonExistingPayee_thenExceptionThrown() {
        doThrow(new EmptyResultDataAccessException(1))
                .when(repository).deleteById(eq(1L));

        assertThrows(PayeeNotFoundException.class, () -> service.delete(1L));
    }

    @Test
    public void whenListing_thenListReturned() {
        Payee payee = Payee.builder().build();
        when(repository.findAll())
                .thenReturn(List.of(payee));

        List<Payee> result = service.list();

        verify(repository).findAll();
        assertThat(result, containsInAnyOrder(payee));
    }
}
