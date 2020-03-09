import java.util.*;

public class DecoderFunction extends Decoder {
    private RDFWriter mRDFWriter;
    private HashSet<String> mUsedFunctions;

    DecoderVariable mDecoderVariable;

    //TODO: function call without asignment

    public DecoderFunction(RDFWriter aRDFWriter, DecoderVariable aDecoderVariable) {
        mRDFWriter = aRDFWriter;
        mUsedFunctions = new HashSet<>();
        mDecoderVariable = aDecoderVariable;
    }

    @Override
    public List<String> process(List<String> aList, int aLevel) {
        //mDecoderVariable.infunctionDeclarationDecoder(aList);

        //aList.remove("(");
        //aList.remove(")");
        clearFunctionCall(aList);

        List<String> result = new ArrayList<>();
        if (aList.size() > 1) {
            functionDecoder(aList);
            result.add(aList.get(1));
        }

        return result;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean checkSequence(String aStr) {
        return Objects.equals(aStr, "(");
    }

    @Override
    public CommandManager.Type getType() {
        return CommandManager.Type.FUNCTION;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void writePack() {
        functionPack();
        userFunctionPack();
        stdFunctionPack();
    }

    private void functionPack() {
        mRDFWriter.write("function", "start", "implement");
        mRDFWriter.write("function", "type", "has_part");
        mRDFWriter.write("function", "type", "return");
    }

    private void userFunctionPack() {
        mRDFWriter.write("user_function", "function", "AKO");
    }

    private void stdFunctionPack() {
        boolean isAnyStdFunkWrited = false;

        MyFileReader fileReader = new MyFileReader("std_functions.txt");
        int groupsCount = Integer.parseInt(fileReader.read());

        for (int i = 0; i < groupsCount; ++i) {
            String parent = fileReader.read();
            int groupSize = Integer.parseInt(fileReader.read());
            boolean isWrited = false;

            for (int j = 0; j < groupSize; ++j) {
                String funkName = fileReader.read();
                if (mUsedFunctions.contains(funkName)) {
                    mRDFWriter.write(funkName, parent + "_function", "ISA");
                    isWrited = true;
                }
            }

            if (isWrited) {
                mRDFWriter.write(parent + "_function", "std_function", "ISA");
                isAnyStdFunkWrited = true;
            }
        }

        if (isAnyStdFunkWrited) {
            mRDFWriter.write("std_function", "function", "AKO");
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private void functionDecoder(List<String> aList) {
        mRDFWriter.write(aList.get(1), aList.get(0), "return");
        mRDFWriter.write(aList.get(1), "user_function", "ISA");
        for (int i = 2; i < aList.size(); ++i) {
            mRDFWriter.write(aList.get(1), aList.get(i + 1), "has_part");
        }
    }
}