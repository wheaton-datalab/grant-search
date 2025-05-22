package org.grants.harvester;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin
public class SearchController {
    
    @PostMapping("/search")
    public List<Grant> search(@RequestBody SearchConfig config) throws Exception{
        return null;
        //return GrantSearcher.run(config);
    }



}
