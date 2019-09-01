package dummydomain.yetanothercallblocker.sia.model.database;

class DatabaseDataSliceNode {

    private DatabaseDataSliceNode[] sliceTree;

    public int init(String data, int offset) {
        this.sliceTree = new DatabaseDataSliceNode[10];

        for (int i = 0; i <= 9; i++) {
            if (data.charAt(offset) == '*' || data.charAt(offset) == '+') {
                offset++;
                continue;
            }

            DatabaseDataSliceNode node = new DatabaseDataSliceNode();
            offset = node.init(data, offset + 1);
            sliceTree[i] = node;
        }
        return offset;
    }

    public int getSliceId(int currentNumber, String number) {
        int currentDigit = Integer.parseInt(number.substring(0, 1));

        number = number.substring(1);

        currentNumber = currentNumber * 10 + currentDigit;

        if (sliceTree[currentDigit] == null) {
            return currentNumber;
        }

        if (number.length() > 0) {
            DatabaseDataSliceNode sliceNode = sliceTree[currentDigit];
            return sliceNode.getSliceId(currentNumber, number);
        }

        // the number is shorter
        return 0;
    }

}
