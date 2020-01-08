package io.openindoormap.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import lombok.Data;

@Data
public class F4D
{
    private Long converter_job_id;
    
    private Integer skinLevel;

    private Integer meshType;

    private Double unitScaleFactor;

    private Boolean createIndex;
    
    private Boolean performOC;

    private String fileName;

    private String inputPath;

    private String outputPath;

    private String logFile;

    private String prefix;

    private String suffix;

    public F4D()
    {
        this.meshType = 0;
        this.skinLevel = 4;
        this.performOC = false;
        this.createIndex = false;
        this.unitScaleFactor = 1.0;
    }

    public List<String> extractArguments()
    {
        List<String> cmd = new ArrayList<String>();

        if(!StringUtils.isEmpty(this.inputPath))
        {
            cmd.add("-inputFolder");
            cmd.add(this.inputPath);
        }

        if(!StringUtils.isEmpty(this.outputPath))
        {
            cmd.add("-outputFolder");
            cmd.add(this.outputPath);
        }
        
        if(!StringUtils.isEmpty(this.logFile))
        {
            cmd.add("-log");
            cmd.add(this.logFile);
        }

        if(!StringUtils.isEmpty(this.prefix))
        {
            cmd.add("-idPrefix");
            cmd.add(this.prefix);
        }

        if(!StringUtils.isEmpty(this.suffix))
        {
            cmd.add("-idSuffix");
            cmd.add(this.suffix);
        }
        
        cmd.add("-usf");
        cmd.add(this.unitScaleFactor.toString());

        cmd.add("-skinLevel");
        cmd.add(this.skinLevel.toString());

        cmd.add("-meshType");
        cmd.add(this.meshType.toString());
        
        cmd.add("-indexing");
        cmd.add(this.createIndex ? "y" : "n");

        cmd.add("-oc");
        cmd.add(this.performOC ? "y" : "n");

        return cmd;
    }
}