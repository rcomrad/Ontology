import java.util.ArrayList;
        import java.util.List;
        import java.util.Objects;

public class DecoderBracketEnd extends Decoder
{
    public DecoderBracketEnd()
    {
    }

    @Override
    public List<String> process(List<String> aList, int aLevel)
    {
        List<String> result = new ArrayList<>();
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public boolean checkSequence(String aStr)
    {
        Objects.equals(aStr, "}");
    }

    @Override
    public CommandManager.Type getType()
    {
        return CommandManager.Type.END;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public void writePack()
    {

    }
}