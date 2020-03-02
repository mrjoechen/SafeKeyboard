package com.keanbin.pinyinime;

import android.inputmethodservice.Keyboard;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

import com.keanbin.pinyinime.pinyinime.DecodingInfo;
import com.keanbin.pinyinime.pinyinime.ImeState;

import java.util.List;

/**
 * Created by chenqiao on 2020-03-02.
 * e-mail : mrjctech@gmail.com
 */
public class PinyinIMEHelper {

    private static final class Holder {
        private static final PinyinIMEHelper INSTANCE = new PinyinIMEHelper();
    }

    public static PinyinIMEHelper getInstance() {
        return Holder.INSTANCE;
    }


    private IPinyinDecoderService mIPinyinDecoderService;

    public PinyinIMEHelper init(IPinyinDecoderService service) {
        mIPinyinDecoderService = service;
        mDecodeing = new DecodingInfo(mIPinyinDecoderService);
        return this;
    }

    private static final String TAG = "PinyinIMEHelper";


    private DecodingInfo mDecodeing;

    private byte mPyBuf[];
    private static final int PY_STRING_MAX = 28;

    private void chooseDecodingCandidate() {
        if (mDecodeing.mImeState != ImeState.STATE_PREDICT) {
            resetCandidates();
            int totalChoicesNum = 0;
            try {
                if (mDecodeing.length() == 0) {
                    totalChoicesNum = 0;
                } else {
                    if (mPyBuf == null)
                        mPyBuf = new byte[PY_STRING_MAX];
                    for (int i = 0; i < mDecodeing.length(); i++)
                        mPyBuf[i] = (byte) mDecodeing.charAt(i); //获得拼音字符串中指定位置的字符
                    mPyBuf[mDecodeing.length()] = 0;

                    totalChoicesNum = mIPinyinDecoderService
                            .imSearch(mPyBuf, mDecodeing.length()); //根据拼音查询候选词

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mDecodeing.mTotalChoicesNum = totalChoicesNum;
        }
    }


    /**
     * 重置候选词列表
     */
    private void resetCandidates() {
        mDecodeing.mCandidatesList.clear();
        mDecodeing.mTotalChoicesNum = 0;
        currentPage = 0;
    }


    public void reset() {
        resetCandidates();
        perSize = 0;
        mDecodeing.resetCandidates();
        mDecodeing.reset();
        if (pinyinInputListener != null) {
            pinyinInputListener.onReset();
        }
    }


    private static final int MAX_PAGE_SIZE_DISPLAY = 16;

    private int perSize = MAX_PAGE_SIZE_DISPLAY;

    /**
     * 删除字符，退格
     */
    public void delChar(){
        inputChar(Keyboard.KEYCODE_DELETE, perSize);
    }


    /**
     * 输入字符
     * @param primaryCode 97 - 'a' ----- 122 'z'
     * @param size
     */
    public void inputChar(int primaryCode, int size) {

        if (size<0 || size > 50){
            perSize = MAX_PAGE_SIZE_DISPLAY;
        }else {
            perSize = size;
        }

        if (primaryCode == Keyboard.KEYCODE_DELETE || primaryCode == -35) {

            mDecodeing.delSplChar((char) primaryCode);
        } else {
            mDecodeing.addSplChar((char) (primaryCode), false);
        }

        chooseDecodingCandidate();

        int fetchStart = mDecodeing.mCandidatesList.size();
        int fetchSize = mDecodeing.mTotalChoicesNum - fetchStart;
        if (fetchSize > perSize) {
            fetchSize = perSize;
        }
        Log.i(TAG, "getCandiagtesForCache fetchStart:" + fetchStart);
        Log.i(TAG, "getCandiagtesForCache fetchSize:" + fetchSize);
        try {
            List<String> newList = null;
            if (ImeState.STATE_INPUT == mDecodeing.mImeState
                    || ImeState.STATE_IDLE == mDecodeing.mImeState
                    || ImeState.STATE_COMPOSING == mDecodeing.mImeState) {
                newList = mIPinyinDecoderService.imGetChoiceList(
                        fetchStart, fetchSize, 0);
            } else if (ImeState.STATE_PREDICT == mDecodeing.mImeState) {
                newList = mIPinyinDecoderService.imGetPredictList(
                        fetchStart, fetchSize);
            }
            mDecodeing.mCandidatesList.addAll(newList);

            if (pinyinInputListener != null) {
                pinyinInputListener.onInputChanged(mDecodeing.getOrigianlSplStr().toString(), mDecodeing.mTotalChoicesNum,
                        perSize, currentPage, mDecodeing.mCandidatesList, newList,fetchSize < perSize);
            }

        } catch (RemoteException e) {
            Log.w(TAG, "PinyinDecoderService died", e);
        }

        while (next()){

        }

    }


    private int currentPage = 0;

    public boolean next() {

        boolean result = false;

        int fetchStart = mDecodeing.mCandidatesList.size();
        int fetchSize = mDecodeing.mTotalChoicesNum - fetchStart;
        if (fetchSize <= 0) return result;
        if (fetchSize > perSize) {
            fetchSize = perSize;
            result = true;
        } else {
            result = false;
        }
        currentPage++;
        Log.i(TAG, "getCandiagtesForCache fetchStart:" + fetchStart);
        Log.i(TAG, "getCandiagtesForCache fetchSize:" + fetchSize);
        try {
            List<String> newList = null;
            if (ImeState.STATE_INPUT == mDecodeing.mImeState
                    || ImeState.STATE_IDLE == mDecodeing.mImeState
                    || ImeState.STATE_COMPOSING == mDecodeing.mImeState) {
                newList = mIPinyinDecoderService.imGetChoiceList(
                        fetchStart, fetchSize, 0);
            } else if (ImeState.STATE_PREDICT == mDecodeing.mImeState) {
                newList = mIPinyinDecoderService.imGetPredictList(
                        fetchStart, fetchSize);
            }
            mDecodeing.mCandidatesList.addAll(newList);

            if (pinyinInputListener != null) {
                pinyinInputListener.onInputChanged(mDecodeing.getOrigianlSplStr().toString(), mDecodeing.mTotalChoicesNum,
                        perSize, currentPage, mDecodeing.mCandidatesList, newList,!result);
            }

//            System.out.println(mDecodeing.mTotalChoicesNum);
//            System.out.println(mDecodeing.mCandidatesList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private PinyinInputListener pinyinInputListener;

    public void setPinyinInputListener(PinyinInputListener pinyinInputListener) {
        this.pinyinInputListener = pinyinInputListener;
    }

    public void removePinyinInputListener(PinyinInputListener pinyinInputListener) {
        this.pinyinInputListener = null;
    }

    public interface PinyinInputListener {

        /**
         * @param pinyin      输入的拼音字符
         * @param tatalSize   拼音匹配结果总数量
         * @param pageSize    分页数量
         * @param currentPage 当前页码
         * @param fullList    当前页码之前全部结果数据
         * @param newList     当前页数据
         */
        void onInputChanged(String pinyin, int tatalSize, int pageSize, int currentPage, List<String> fullList, List<String> newList, boolean isLast);

        void onReset();

    }


    //todo *********************************** 分割线 以下代码待完善，请勿调用 *****************************************

//    private ImeState mImeState = ImeState.STATE_IDLE;
//
//
//    /**
//     * 添加输入的拼音，然后进行词库查询，或者删除输入的拼音指定的字符或字符串，然后进行词库查询。
//     *
//     * @param keyChar
//     * @param keyCode
//     * @return
//     */
//    private boolean processSurfaceChange(int keyChar, int keyCode) {
//        if (mDecodeing.isSplStrFull() && KeyEvent.KEYCODE_DEL != keyCode) {
//            return true;
//        }
//
//        if ((keyChar >= 'a' && keyChar <= 'z')
//                || (keyChar == '\'' && !mDecodeing.charBeforeCursorIsSeparator())
//                || (((keyChar >= '0' && keyChar <= '9') || keyChar == ' ') && ImeState.STATE_COMPOSING == mImeState)) {
//            mDecodeing.addSplChar((char) keyChar, false);
//            chooseAndUpdate(-1);
//        } else if (keyCode == Keyboard.KEYCODE_DELETE || keyCode == -35) {
//            mDecodeing.prepareDeleteBeforeCursor();
//            chooseAndUpdate(-1);
//        }
//        return true;
//    }
//
//
//    private int mCurrentPage = -1;
//
//    /**
//     * 选择候选词，并根据条件是否进行下一步的预报。
//     *
//     * @param candId
//     *            如果candId小于0 ，就对输入的拼音进行查询。
//     */
//    private void chooseAndUpdate(int candId) {
//        Log.i(TAG,"chooseAndUpdate... mImeState:"+mImeState);
//
//        if (ImeState.STATE_PREDICT != mImeState) {
//            // Get result candidate list, if choice_id < 0, do a new decoding.
//            // If choice_id >=0, select the candidate, and get the new candidate
//            // list.
//            mDecodeing.chooseDecodingCandidate(candId);
//        } else {
//            // Choose a prediction item.
//            mDecodeing.choosePredictChoice(candId);
//        }
//
//        Log.i(TAG,"chooseAndUpdate... length:"+mDecodeing.getComposingStr().length()+" mDecInfo.getComposingStr:"+mDecodeing.getComposingStr());
//        System.out.println(mDecodeing.mTotalChoicesNum);
//
//        while (mDecodeing.preparePage(mCurrentPage + 1)){
//            mCurrentPage++;
//
//            int count = 0;
//            for (int i = 0; i < mDecodeing.mPageStart.size(); i++){
//                count = count + mDecodeing.mPageStart.get(i);
//            }
//            mDecodeing.mPageStart.add(mDecodeing.mCandidatesList.size());
//
//        }
//
//        System.out.println(mDecodeing.mCandidatesList);
//
//        mDecodeing.resetCandidates();
//        mCurrentPage = 0;


//        if (mDecodeing.getComposingStr().length() > 0) {
//            String resultStr;
//            // 获取选择了的候选词
//            resultStr = mDecodeing.getComposingStrActivePart();
//            Log.i(TAG,"chooseAndUpdate... resultStr:"+resultStr);
//            Log.i(TAG,"chooseAndUpdate... candId:"+candId);
//            Log.i(TAG,"chooseAndUpdate... mDecInfo.canDoPrediction():"+mDecodeing.canDoPrediction());
//            // choiceId >= 0 means user finishes a choice selection.
//            if (candId >= 0 && mDecodeing.canDoPrediction()) {
//                // 发生选择了的候选词给EditText
//                commitResultText(resultStr);
//                // 设置输入法状态为预报
//                mImeState = ImeState.STATE_PREDICT;
//                // TODO 这一步是做什么？
//                if (null != mSkbContainer && mSkbContainer.isShown()) {
//                    mSkbContainer.toggleCandidateMode(false);
//                }
//
//                // Try to get the prediction list.
//                // 获取预报的候选词列表
//                if (mDecodeing.isPrediction) {
//                    InputConnection ic = getCurrentInputConnection();
//                    if (null != ic) {
//                        CharSequence cs = ic.getTextBeforeCursor(3, 0);
//                        if (null != cs) {
//                            mDecodeing.preparePredicts(cs);
//                        }
//                    }
//                } else {
//                    mDecodeing.resetCandidates();
//                }
//
//                if (mDecodeing.mCandidatesList.size() > 0) {
//                    showCandidateWindow(false);
//                } else {
//                    resetToIdleState(false);
//                }
//            } else {
//                if (ImeState.STATE_IDLE == mImeState) {
//                    if (mDecodeing.getSplStrDecodedLen() == 0) {
//                        changeToStateComposing(true);
//                    } else {
//                        changeToStateInput(true);
//                    }
//                } else {
//                    if (mDecodeing.selectionFinished()) {
//                        changeToStateComposing(true);
//                    }
//                }
//                showCandidateWindow(true);
//            }
//        } else {
//            resetToIdleState(false);
//        }
//    }
}
