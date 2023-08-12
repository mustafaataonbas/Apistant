import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

class Apistant{
    public static void main(String[] args) throws IOException, JSONException {
        String doc_url = null;
        int detail = 0;
        int menuInput = 0;
        System.out.print("\n");
        System.out.print("Welcome to API Documentation Assistant - v. 2.21.22" + "\n");
        System.out.print("This program aims to analyze HTTP methods from a specific API documentation base URL" + "\n" + "\n");
        do {
            System.out.print("/////////////// MENU ///////////////" + "\n");
            menu();
            Scanner userInput = new Scanner(System.in);
            System.out.print("Action: ");
            if(userInput.hasNextInt()){
                menuInput = userInput.nextInt();
            }
            if(menuInput != 1 && menuInput != 2){
                System.out.print("Invalid entry! Please try again" + "\n");
            }
            System.out.print("\n");
            if(menuInput == 2) {
                System.out.print("/////////////// HELP ///////////////" + "\n");
                help();
            }
        }
        while (menuInput != 1 && menuInput != 2);
        
        do {
            Scanner userInput = new Scanner(System.in);
            System.out.print("Please enter the URL address of the API documentation: ");
            if (userInput.hasNextLine()) {
                doc_url = userInput.nextLine();
            }
            System.out.print("Please enter the detail type: ");
            if (userInput.hasNextInt()) {
                detail = userInput.nextInt();
            }
            if((Objects.requireNonNull(doc_url).lastIndexOf('.') == -1) || !doc_url.substring(doc_url.lastIndexOf('.')).equals(".json") || (detail != 1 && detail != 2 && detail != 3 && detail != 4)){
                System.out.print("Invalid entry! Please try again" + "\n" + "\n");
            }
        }
        while(((Objects.requireNonNull(doc_url).lastIndexOf('.') == -1)) || ((!doc_url.substring(doc_url.lastIndexOf('.')).equals(".json")) || (detail != 1 && detail != 2 && detail != 3 && detail != 4)));

        String result = "";
        String myText = "";
        try{
            URL url = new URL(doc_url);
            Scanner sc = new Scanner(url.openStream());
            StringBuilder sb = new StringBuilder();
            while(sc.hasNext()) {
                sb.append(sc.next());
                sb.append(' ');
            }
            result = sb.toString();
            sc.close();
        }
        catch(IOException x){
            throw new IOException("Unavailable: "+x.getMessage(),x);
        }

        try {
            JSONObject json = new JSONObject(result);
            myText = json.toString(0);
        }
        catch (JSONException y) {
            throw new JSONException("Text is not convertible to a json object.");
        }

        //System.out.print(myText+ "\n");
        //System.out.print("\n");

        String api_name_ = "";
        String method_ = "";
        String summary_ = "";
        boolean deprecated_ = false;
        int totalHttpMethods;

        LinkedList methodsList = new LinkedList();

        Scanner scanner = new Scanner(myText);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            int key_word_index = line.indexOf("\"", 1);
            if (key_word_index != -1 && line.substring(1, key_word_index).equals("paths")){ //That means after the "paths" keyword, we'll be seeing consecutive HTTP methods
                while(!line.equals("definitions") && scanner.hasNextLine()){ //Until the end of HTTP methods
                    line = scanner.nextLine();
                    key_word_index = line.indexOf("\"", 1);
                    if (key_word_index != -1) {
                        String key = line.substring(1, key_word_index);

                        if ((key.charAt(0) == '\\' ||  isExist(key)) && (!api_name_.isEmpty() && !method_.isEmpty() && !summary_.isEmpty())) { //Current method has been read. Ready variables for next one
                            LinkedList.insert(methodsList, api_name_, method_, summary_, deprecated_);
                            String dummy = api_name_;
                            api_name_ = "";
                            method_ = "";
                            summary_ = "";
                            deprecated_ = false;
                            if(isExist(key)){
                                key_word_index = line.indexOf("\"", 1);
                                method_ = line.substring(1, key_word_index);
                                api_name_ = dummy;
                            }
                        }
                        if (key.charAt(0) == '\\') {
                            key = key.replaceAll("\\\\", "");
                            api_name_ = key;
                            if(scanner.hasNextLine()){
                                line = scanner.nextLine();
                                key_word_index = line.indexOf("\"", 1);
                                method_ = line.substring(1, key_word_index);
                            }
                        }
                        else if (key.equals("summary")) {
                            key_word_index = line.indexOf(":");
                            summary_ = line.substring(key_word_index + 2);
                        }
                        else if (key.equals("deprecated")) {
                            key_word_index = line.indexOf(":");
                            String deprecated_bool = line.substring(key_word_index + 2);
                            if (deprecated_bool.equals("true,")) {
                                deprecated_ = true;
                            }
                        }
                    }
                }
            }
        }
        if (!api_name_.isEmpty() && !method_.isEmpty() && !summary_.isEmpty()) {
            LinkedList.insert(methodsList, api_name_, method_, summary_, deprecated_);
        }
        scanner.close();
        if(detail == 1){
            LinkedList dum = new LinkedList();
            LinkedList.copyConstructor(methodsList, dum);
            LinkedList.detailedOutput(methodsList, dum);
            System.out.print("//////////////////////////////////////////////////" + "\n");
            LinkedList.classBasedHttpMethodCount(methodsList);
            totalHttpMethods = LinkedList.httpMethodCount(methodsList);
            System.out.print("//////////////////////////////////////////////////" + "\n");
            System.out.print("Total active HTTP methods reside in this API is: " + totalHttpMethods + "\n");
        }
        else if(detail == 2){
            LinkedList.classBasedHttpMethodCount(methodsList);
        }
        else if(detail == 3){
            totalHttpMethods = LinkedList.httpMethodCount(methodsList);
            System.out.print("Total active HTTP methods reside in this API is: " + totalHttpMethods + "\n");

            LinkedList dum_list = new LinkedList();
            LinkedList.copyConstructor(methodsList, dum_list);
            LinkedList.specificHttpMethodCount(methodsList, dum_list);
        }
        else {
            LinkedList.display(methodsList);
            totalHttpMethods = LinkedList.httpMethodCount(methodsList);
            System.out.print("//////////////////////////////////////////////////" + "\n");
            System.out.print("Total active HTTP methods reside in this API is: " + totalHttpMethods + "\n");

        }
    }

    public static boolean isExist(String method){
        String[] methodList = new String[]{"get", "put", "post", "delete", "update", "acl", "baseline-control" , "bind",
                "checkin", "checkout", "connect", "copy", "head", "label" , "link", "lock", "merge", "mkactivity", "mkcalendar", "mkcol",
                "mkredirectref", "mkworkspace", "move", "options", "orderpatch", "patch", "pri", "propfind", "proppatch", "rebind", "report",
                "search", "trace", "unbind", "uncheckout", "unlink", "unlock", "updaterdirectref", "version-control"};
        for (String element : methodList){
            if(element.equals(method)){
                return true;
            }
        }
        return false;
    }

    public static void help(){
        System.out.print("This program functions with two user inputs:" + "\n");
        System.out.print("1 -> Base URL of the API Documentation " + " 2 -> Output detail type(1 , 2 , 3 ,4)" + "\n" + "\n");
        System.out.print("CAUTION!" + "\n");
        System.out.print("Requirements in terms of inputs to get a correct output: " + "\n" + "\n");
        System.out.print("***The URL address of the API must be .json formatted on the URL:" + "\n");
        System.out.print("  --> Example format of the URL address(This is an active link! Please open the link to clarify the inside look of the URL address): https://petstore.swagger.io/v2/swagger.json" + "\n" + "\n");
        System.out.print("***Detail type must be an integer between 1-4:" + "\n");
        System.out.print("  --> 1. For most detailed output" + "\n");
        System.out.print("  --> 2. For class based output" + "\n");
        System.out.print("  --> 3. Summary" + "\n");
        System.out.print("  --> 4. List of the whole HTTP Methods" + "\n");
        System.out.print("If the documentation has massive number of http methods, these operations might take some time" + "\n" + "\n");
    }
    
    public static void menu(){
        System.out.print("Type 1 - To proceed" + "\n");
        System.out.print("Type 2 - Access help document" + "\n");
    }
}