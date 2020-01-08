package io.openindoormap.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.openindoormap.config.AMQPConfig;
import io.openindoormap.config.PropertiesConfig;
import io.openindoormap.domain.ConverterJob;
import io.openindoormap.domain.F4D;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class F4DConverter {
    @Autowired
    private PropertiesConfig propertiesConfig;

    @Autowired
    private RestTemplate restTemplate;

    @RabbitListener(queues = AMQPConfig.INCOMING_QUEUE_NAME)
    public void receive(F4D f4d) {
        List<String> command = new ArrayList<String>();
        command.add("F4DConverter");
        command.addAll(f4d.extractArguments());

        String[] cmd = command.stream().toArray(String[]::new);
        System.out.println("Command = " + Arrays.toString(cmd));
        updateConverterStatus(f4d, ConverterJob.JOB_RUNNING, null);
        try {
            byProcessBuilderRedirect(cmd);
            updateConverterStatus(f4d, ConverterJob.JOB_SUCCESS, null);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            updateConverterStatus(f4d, ConverterJob.JOB_FAIL, e.toString());
        }
        System.out.println("Received message = "+ f4d);



        //Random generator = new Random(); 
        //int sleepTime = generator.nextInt(10) * 10000;
        //Thread.sleep(sleepTime);
        //System.out.println(sleepTime + "Received message = "+ f4d);
    }
    /*
    public void byCommonsExec(String[] command) throws IOException,InterruptedException
    {
        DefaultExecutor executor = new DefaultExecutor();
        CommandLine cmdLine = CommandLine.parse(command[0]);
        for (int i=1, n=command.length ; i<n ; i++ ) {
            cmdLine.addArgument(command[i]);
        }
        executor.execute(cmdLine);
    }
    */
    public void byProcessBuilderRedirect(String[] command) throws IOException, InterruptedException
    {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectOutput(Redirect.INHERIT);
        builder.redirectError(Redirect.INHERIT);
        Process process = builder.start();
        printStream(process);
    }

    private void printStream(Process process) throws IOException, InterruptedException
    {
        process.waitFor();
        try (InputStream psout = process.getInputStream())
        {
            copy(psout, System.out);
        }
    }

    public void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int n = 0;
        while ((n = input.read(buffer)) != -1) {
            output.write(buffer, 0, n);
        }
    }

	private void updateConverterStatus(F4D f4d, String status, String result) {
        ConverterJob dto = new ConverterJob();
        
        dto.setConverter_job_id(f4d.getConverter_job_id());
        dto.setStatus(status);
        if(!StringUtils.isEmpty(result)) {
            dto.setError_code(result);
        }
        
		URI url;
        try {
            url = new URI(propertiesConfig.getRestServer() + "/api/converter/status");
            restTemplate.postForEntity(url, dto, String.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
	}
}