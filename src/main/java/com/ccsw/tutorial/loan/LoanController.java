package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Loan", description = "API of Loan")
@RequestMapping(value = "/loan")
@RestController
@CrossOrigin(origins = "*")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private ModelMapper mapper;

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public LoanDto get(@PathVariable Long id) {
        Loan loan = loanService.get(id);
        return mapper.map(loan, LoanDto.class);
    }

    @RequestMapping(path = "", method = RequestMethod.GET)
    public List<LoanDto> findAll() {
        List<Loan> loans = loanService.findAll();
        return loans.stream().map(e -> mapper.map(e, LoanDto.class)).collect(Collectors.toList());
    }

    @RequestMapping(path = "", method = RequestMethod.POST)
    public Page<LoanDto> findPage(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "client", required = false) String client,
            @RequestParam(value = "date", required = false) Date date,
            @RequestBody LoanSearchDto dto
    ) {
        Page<Loan> page = loanService.findPage(title, client, date, dto);
        return new PageImpl<>(page.getContent().stream().map(e -> mapper.map(e, LoanDto.class)).collect(Collectors.toList()), page.getPageable(), page.getTotalElements());
    }

    @RequestMapping(path = { "", "/{id}" }, method = RequestMethod.PUT)
    public void save(@RequestParam(required = false) Long id, @RequestBody LoanDto dto) {
        loanService.save(id, dto);
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable Long id) {
        loanService.delete(id);
    }

}

