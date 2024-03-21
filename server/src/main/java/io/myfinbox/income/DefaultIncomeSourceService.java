package io.myfinbox.income;

import io.myfinbox.shared.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;

@Service
@Transactional
@RequiredArgsConstructor
class DefaultIncomeSourceService implements IncomeSourceService {

    private final IncomeSources incomeSources;

    @Override
    public Either<Failure, List<IncomeSource>> createDefault(AccountIdentifier account) {
        if (isNull(account)) {
            return Either.left(Failure.ofValidation("AccountIdentifier cannot be null", List.of()));
        }

        var values = DefaultIncomeSources.asList().stream()
                .map(is -> new IncomeSource(is, account))
                .toList();

        incomeSources.saveAll(values);

        return Either.right(values);
    }
}
