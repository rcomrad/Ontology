import java.util.*;

public class ProgramDecoder {
    private MyFileReader mFileReader;
    private MyFileWriter mFileWriter;

    private int mAssignmentCounter;
    private int mConditionCounter;

    private HashSet<String> mUsedFunctions;
    private HashSet<String> mUsedTypes;

    ArrayList<Pair<String, String>> codeLevel;

    public ProgramDecoder() {
        mFileReader = new MyFileReader("sqr_equ_code.cpp");
        mFileWriter = new MyFileWriter("rdf code");

        mAssignmentCounter = 0;
        mConditionCounter = 0;

        mUsedFunctions = new HashSet<>();
        mUsedTypes = new HashSet<>();

        codeLevel = new ArrayList<>();
        codeLevel.add(new Pair("start", "include"));
    }

    private void startPack() {
        mFileWriter.write(pack("start", "function", "implement"));
        mFileWriter.write(pack("user_functions", "function", "AKO"));
        mFileWriter.write(pack("start", "types", "implement"));
        mFileWriter.write(pack("basic", "types", "AKO"));
    }

    private void writeLever(String str) {
        mFileWriter.write(pack(str, codeLevel.get(codeLevel.size() - 1).mX,
                codeLevel.get(codeLevel.size() - 1).mY));
    }

    private void variablePack() {
        mFileWriter.write(pack("start", "variable", "implement"));
    }

    private void inputStreamPack() {
        mFileWriter.write(pack("start", "input_stream", "implement"));
        mFileWriter.write(pack("cin", "input_stream", "ISA"));
        mFileWriter.write(pack("cout", "input_stream", "ISA"));
    }

    private void conditionPack() {
        mFileWriter.write(pack("start", "condition", "implement"));
        mFileWriter.write(pack("if", "condition", "ISA"));
        mFileWriter.write(pack("if-else", "condition", "ISA"));
        mFileWriter.write(pack("if-else_tree", "condition", "ISA"));
    }

    private void userFunctionsPack() {
        mFileWriter.write(pack("start", "function", "implement"));
    }

    public void process() {
        List<String> list = new ArrayList();
        int type = 0;
        boolean variablelsUsed = false;
        boolean streamUsed = false;

        startPack();

        String str;
        while (!Objects.equals(str = mFileReader.read(), "")) {
            if (isEndSequence(str)) {
                //stackParser(list);
                if (type == 3 && list.size() > 0) functionDecoder(list);
                else if ((type & 1) != 0) {
                    variablelsUsed = true;
                    variableDeclarationDecoder(list);
                }

                if ((type & 4) != 0) variableAssignmentDecoder(list);
                //if ((type & 4) != 0) variableDeclarationDecoder(list);

                if ((type & 8) != 0) streamDecoder(list);

                if (isLevelIncreaser(str)) {
                    codeLevel.add(new Pair(list.get(1), "include"));
                }

                if (isLevelDecreaser(str)) {
                    codeLevel.remove(codeLevel.size() - 1);
                }

                list.clear();
                type = 0;
            } else {
                if (isTypeSequence(str) && list.size() == 0) {
                    mUsedTypes.add(str);
                    type |= 1;
                }
                if (isFunctionalSequence(str)) {
                    if ((type & 1) != 1) mUsedFunctions.add(list.get(list.size() - 1));
                    type |= 2;
                }
                if (isAssignmentSequence(str)) {
                    variablelsUsed = true;
                    type |= 4;
                }
                if (isStreamSequence(str)) {
                    type |= 8;
                    streamUsed = true;
                }

                if (!isUnusedSequence(str)) list.add(str);
            }
        }

        if (mUsedTypes.size() > 0) typesDecoder();
        if (variablelsUsed) variablePack();
        if (streamUsed) inputStreamPack();
        if (mUsedFunctions.size() > 0)
        {
            stdFunctionPack();
        }

        mFileWriter.close();
    }

    private void typesDecoder() {
        MyFileReader fileReader = new MyFileReader("basic_types.txt");
        int count = Integer.parseInt(fileReader.read());
        for (int i = 0; i < count; ++i) {
            String parent = fileReader.read();
            String concept = fileReader.read();
            String connection = fileReader.read();
            if (mUsedTypes.contains(parent)) mFileWriter.write(pack(parent, concept, connection));
        }
    }

    private void stdFunctionPack() {
        mFileWriter.write(pack("std_functions", "function", "AKO"));
        MyFileReader fileReader = new MyFileReader("std_functions.txt");
        int groupsCount= Integer.parseInt(fileReader.read());

        for (int i = 0; i < groupsCount; ++i) {
            String parent = fileReader.read();
            int groupsSize= Integer.parseInt(fileReader.read());
            boolean isFirstWrite = true;
            for(int j = 0; j < groupsSize; ++j)
            {
                String funkName = fileReader.read();
                if (mUsedFunctions.contains(funkName))
                {
                    if (isFirstWrite)
                    {
                        isFirstWrite = false;
                        mFileWriter.write(pack(parent + "_functions", "std_functions", "AKO"));
                    }
                    mFileWriter.write(pack(funkName, parent + "_functions", "ISA"));
                }
            }
        }
    }

    public boolean isLevelIncreaser(String str) {
        return Objects.equals(str, "{");
    }

    public boolean isStreamSequence(String str) {
        return Objects.equals(str, "cin") || Objects.equals(str, "cout");
    }

    public boolean isLevelDecreaser(String str) {
        return Objects.equals(str, "}");
    }

    public boolean isEndSequence(String str) {
        return Objects.equals(str, ";") || Objects.equals(str, "{") || Objects.equals(str, "}");
    }

    public boolean isTypeSequence(String s) {
        return Objects.equals(s, "int") || Objects.equals(s, "float") || Objects.equals(s, "double")
                || Objects.equals(s, "char") || Objects.equals(s, "bool") || Objects.equals(s, "void");
    }

    public boolean isAssignmentSequence(String s) {
        return Objects.equals(s, "=");
    }

    public boolean isFunctionalSequence(String s) {
        return Objects.equals(s, "(");
    }

    public boolean isIfSequence(String s) {
        return Objects.equals(s, "if");
    }

    public boolean isElseSequence(String s) {
        return Objects.equals(s, "else");
    }

    public boolean isElseIfSequence(String s1, String s2) {
        return Objects.equals(s1, "else") && Objects.equals(s2, "if");
    }

    public boolean isUnusedSequence(String s) {
        return Objects.equals(s, ",") || Objects.equals(s, "(") || Objects.equals(s, ")") ||
                Objects.equals(s, "=") || Objects.equals(s, "+") || Objects.equals(s, ">") ||
                Objects.equals(s, "<") || Objects.equals(s, "&&") || Objects.equals(s, "||") ||
                Objects.equals(s, "!=") || Objects.equals(s, "==");
    }

    public void stackParser(List<String> aList) {
        //for (List<String> entry : node.mConnections.entrySet()) {
        //    OntologyNode nextNode = entry.getValue().getSecond();
        //}
        //aList[0] = "hh";
    }

    private String pack(String s1, String s2, String s3) {
        return s1 + " " + s2 + " " + s3 + "\n";
    }

    public void functionDecoder(List<String> aList) {
        writeLever(aList.get(1));

        mFileWriter.write(pack(aList.get(1), aList.get(0), "return"));
        mFileWriter.write(pack(aList.get(1), "user_functions", "ISA"));
        for (int i = 2; i < aList.size(); i += 2) {
            mUsedTypes.add(aList.get(i));
            mFileWriter.write(pack(aList.get(1), aList.get(i + 1), "take"));
            mFileWriter.write(pack(aList.get(i + 1), aList.get(i), "has_type"));
            mFileWriter.write(pack(aList.get(i + 1), "variable", "ISA"));
        }
    }

    public void conditionDecoder(List<String> aList) {
        if (isIfSequence(aList.get(0)))
        {
            
        }

        writeLever(aList.get(1));
        if
        mFileWriter.write(pack(aList.get(1), aList.get(0), "return"));
        mFileWriter.write(pack(aList.get(1), "user_functions", "ISA"));
        for (int i = 2; i < aList.size(); i += 2) {
            mUsedTypes.add(aList.get(i));
            mFileWriter.write(pack(aList.get(1), aList.get(i + 1), "take"));
            mFileWriter.write(pack(aList.get(i + 1), aList.get(i), "has_type"));
            mFileWriter.write(pack(aList.get(i + 1), "variable", "ISA"));
        }
    }

    public void streamDecoder(List<String> aList) {
        //writeLever(aList.get(0));
        //mFileWriter.write(pack(aList.get(0), "input_stream", "ISA"));
        for (int i = 2; i < aList.size(); i += 2) {
            String valueName = "value" + mAssignmentCounter++;
            writeLever(valueName);
            mFileWriter.write(pack(aList.get(i), valueName, "assignment"));
            mFileWriter.write(pack(aList.get(0), valueName, "read"));
        }
    }

    public void variableDeclarationDecoder(List<String> aList) {
        for (int i = 1; i < aList.size(); ++i) {
            writeLever(aList.get(i));
            mFileWriter.write(pack(aList.get(i), aList.get(0), "has_type"));
            mFileWriter.write(pack(aList.get(i), "variable", "ISA"));
        }
    }

    public void variableAssignmentDecoder(List<String> aList) {
        String valueName = "value" + mAssignmentCounter++;
        writeLever(valueName);
        mFileWriter.write(pack(aList.get(0), valueName, "assignment"));
        boolean usesNumeric = false;

        for (int i = 1; i < aList.size(); ++i) {
            if (aList.get(i).codePointAt(0) >= 'A' && aList.get(i).codePointAt(0) <= 'Z' ||
                    aList.get(i).codePointAt(0) >= 'a' && aList.get(i).codePointAt(0) <= 'z')
                mFileWriter.write(pack(valueName, aList.get(i), "use"));
            else usesNumeric = true;
        }

        if (usesNumeric) {
            mFileWriter.write(pack(valueName, "numeric", "use"));
        }
    }
}