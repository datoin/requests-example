package org.datoin.net.example;

import org.apache.commons.cli.*;
import org.datoin.net.http.Request;
import org.datoin.net.http.Requests;
import org.datoin.net.http.Response;
import org.datoin.net.http.methods.Methods;

import java.io.File;
import java.util.Arrays;

/**
 * Author : umarshah@simplyphi.com
 * Created on : 30/6/14.
 */
public class Main {
    private static final String METHOD = "method";
    private static final String URL = "url";
    private static final String PARAMS = "params";
    private static final String FILES = "files";
    public static final int MAX_ARGS = 20;
    private static final String CONTENT = "content";
    private static final String HEADERS = "headers";

    public static void main(String[] args) throws Exception {
        Request request = parseRequest(args);
        Response resp = request.execute();
        int status = resp.getStatus();
        if( status >= 200 && status < 400){ // allow ok and redirects
            String responseString = resp.getContentAsString();
            if(responseString != null) {
                System.out.println(responseString);
            }
        }
    }

    public static Request parseRequest(String[] args) throws Exception {
        Options options = new Options();
        //options.addOption("o", "output",  false, "output path");
        options.addOption(OptionBuilder
                .withLongOpt(METHOD)
                .withDescription("http method to use")
                .hasArg()
                .isRequired()
                .withArgName(METHOD.toUpperCase())
                .create(METHOD.substring(0, 1)));
        options.addOption(OptionBuilder
                .withLongOpt(URL)
                .withDescription("target url")
                .hasArg()
                .isRequired()
                .withArgName(URL.toUpperCase())
                .create(URL.substring(0, 1)));
        options.addOption(OptionBuilder
                .withLongOpt(PARAMS)
                .withDescription("optional params as name1=value1 name2=value2 ...")    // implement json and text
                .hasArgs(MAX_ARGS)
                .withArgName(PARAMS.toUpperCase())
                .create(PARAMS.substring(0, 1)));
        options.addOption(OptionBuilder
                .withLongOpt(FILES)
                .hasArgs(MAX_ARGS)
                .withDescription("optional multipart file uploads , file1 file2 ...")
                .withArgName(FILES.toUpperCase())
                .create(FILES.substring(0, 1)));
        options.addOption(OptionBuilder
                        .withLongOpt(CONTENT)
                        .hasArg()
                        .withDescription("optional file to use content to send in http request")
                        .withArgName(CONTENT.toUpperCase())
                        .create(CONTENT.substring(1, 2))
        );
        options.addOption(OptionBuilder
                        .withLongOpt(HEADERS)
                        .hasArg()
                        .withDescription("optional headers to be set as header1=value1 header2=value2 ...")
                        .withArgName(HEADERS.toUpperCase())
                        .create(HEADERS.substring(0, 1))
        );
        HelpFormatter formatter = new HelpFormatter();

        if (args.length < 4) {
            System.out.println("less number of arguments than expected, refer usage below");
            formatter.printHelp("importer", options);
            System.exit(1);
        } else {
            System.out.println(Arrays.toString(args));
            CommandLineParser parser = new PosixParser();
            CommandLine cmd = null;
            StringBuilder sb = new StringBuilder("Your arguments are : \n");
            try {
                cmd = parser.parse(options, args);
                String method = cmd.getOptionValue(METHOD);
                String contentFile = cmd.getOptionValue(CONTENT, null);
                String[] params = cmd.getOptionValues(PARAMS);
                String url = cmd.getOptionValue(URL);
                String files[]  = cmd.getOptionValues(FILES);
                String headers[] = cmd.getOptionValues(HEADERS);
                Request request = getRequest(method, url);
                sb.append(METHOD).append(" = ").append(method).append("\n")
                        .append(URL).append(" = ").append(url).append("\n")
                        .append(CONTENT).append(" = ").append(contentFile).append("\n");

                if(params != null) {
                    sb.append(PARAMS).append(" = ").append(Arrays.asList(params)).append("\n");
                    for(String param : params){
                        String[] split = param.split("=");
                        request.setParam(split[0], split[1]);
                    }
                }
                if(files != null) {
                    sb.append(FILES).append(" = ").append(Arrays.asList(files)).append("\n");
                    for(String file : files ){
                        request.addInputStream(file, new File(file));
                    }
                }

                if(headers != null) {
                    sb.append(HEADERS).append(" = ").append(Arrays.asList(headers));
                    for(String header : headers){
                        String[] split = header.split("=");
                        request.setHeader(split[0], split[1]);
                    }
                }
                System.out.println("Prepared request using : \n" + sb.toString());
                return request;

            } catch (Exception e) {
                System.out.println("Arguments mismatched: " + e);
                formatter.printHelp("importer", options);
                throw e;
            }
        }
        return null;
    }

    private static Request getRequest(String method, String url){

        if (method.equalsIgnoreCase(Methods.GET.getMethod())){
            return Requests.get(url);
        } else if (method.equalsIgnoreCase(Methods.POST.getMethod())){
            return Requests.post(url);
        } else if (method.equalsIgnoreCase(Methods.PUT.getMethod())){
            return Requests.put(url);
        } else if (method.equalsIgnoreCase(Methods.DELETE.getMethod())){
            return Requests.delete(url);
        } else if (method.equalsIgnoreCase(Methods.HEAD.getMethod())){
            return Requests.head(url);
        } else {
            return null;
        }
    }
}
