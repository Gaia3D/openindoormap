package io.openindoormap.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.openindoormap.config.AMQPConfig;
import io.openindoormap.domain.F4D;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class F4DConverter
{
    @RabbitListener(queues = AMQPConfig.INCOMING_QUEUE_NAME)
    public void receive(F4D f4d) throws InterruptedException, IOException {
        List<String> command = new ArrayList<String>();
        command.add("F4DConverter");
        command.addAll(f4d.extractArguments());

        String[] cmd = command.stream().toArray(String[]::new);
        System.out.println("Command = "+ Arrays.toString(cmd));
        byProcessBuilderRedirect(cmd);
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
}