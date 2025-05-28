package org.grants.harvester;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class SearchController {
    
    @PostMapping("/search")
    public List<Grant> search(@RequestBody SearchConfig config) throws Exception{
        return GrantSearcher.run(config);
    }

}
