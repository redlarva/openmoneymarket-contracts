package finance.omm.score.core.reward.distribution;

import static finance.omm.utils.math.MathUtils.ICX;
import static finance.omm.utils.math.MathUtils.exaDivide;
import static finance.omm.utils.math.MathUtils.exaMultiply;

import finance.omm.libs.address.Contracts;
import finance.omm.libs.structs.AssetConfig;
import finance.omm.libs.structs.DistPercentage;
import finance.omm.libs.structs.TypeWeightStruct;
import finance.omm.libs.structs.UserDetails;
import finance.omm.libs.structs.WeightStruct;
import finance.omm.score.core.reward.distribution.exception.RewardDistributionException;
import finance.omm.score.core.reward.distribution.model.Asset;
import finance.omm.utils.math.MathUtils;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import score.Address;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import scorex.util.HashMap;

public class RewardDistributionImpl extends AbstractRewardDistribution {

    public static final String TAG = "Reward distribution";
    public static final String DAY = "day";
    public static final String IS_INITIALIZED = "isInitialized";
    public static final String IS_REWARD_CLAIM_ENABLED = "isRewardClaimEnabled";


    public final VarDB<BigInteger> distributedDay = Context.newVarDB(DAY, BigInteger.class);
    public final VarDB<Boolean> _isInitialized = Context.newVarDB(IS_INITIALIZED, Boolean.class);
    public final VarDB<Boolean> _isRewardClaimEnabled = Context.newVarDB(IS_REWARD_CLAIM_ENABLED, Boolean.class);


    public RewardDistributionImpl(Address addressProvider, BigInteger _weight) {
        super(addressProvider, _weight);
    }

    @External(readonly = true)
    public String name() {
        return "OMM " + TAG;
    }

    @Override
    @External(readonly = true)
    public Map<String, BigInteger> getAssetEmission() {
        return null;
    }

    @Override
    @External(readonly = true)
    public List<Address> getAssets() {
        return this.assets.keySet();
    }

    @Override
    @External(readonly = true)
    public Map<String, String> getAssetNames() {
        return this.assets.getAssetName();
    }

    @Override
    @External(readonly = true)
    public Map<String, BigInteger> getIndexes(Address _user, Address _asset) {
        return Map.of("userIndex", this.assets.getUserIndex(_asset, _user)
                , "assetIndex", this.assets.getAssetIndex(_asset));
    }

    @External(readonly = true)
    public BigInteger getAssetIndex(Address _asset) {
        return this.assets.getAssetIndex(_asset);
    }

    @External(readonly = true)
    public BigInteger getLastUpdatedTimestamp(Address _asset) {
        return this.assets.getLastUpdateTimestamp(_asset);
    }


    @Override
    @Deprecated
    @External
    public void setAssetName(Address _asset, String _name) {
        Context.println("setAssetName-called" + Context.getCaller());
    }


    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#setTypeWeight(TypeWeightStruct[],
     * BigInteger)}
     */
    @Override
    @Deprecated
    @External
    public void setDistributionPercentage(DistPercentage[] _distPercentage) {
        Context.println("setDistributionPercentage-called" + Context.getCaller());
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getTypeWeight(String,
     * BigInteger)}
     */
    @Override
    @Deprecated
    @External(readonly = true)
    public BigInteger getDistributionPercentage(String _recipient) {
        return call(BigInteger.class, Contracts.REWARD_WEIGHT_CONTROLLER, "getTypeWeight", _recipient);
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getALlTypeWeight(BigInteger)}
     */
    @Override
    @External(readonly = true)
    @Deprecated
    public Map<String, BigInteger> getAllDistributionPercentage() {
        return call(Map.class, Contracts.REWARD_WEIGHT_CONTROLLER, "getALlTypeWeight");
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getAssetWeight(Address,
     * BigInteger)}
     */
    @Override
    @Deprecated
    @External(readonly = true)
    public BigInteger assetDistPercentage(Address asset) {
        return call(BigInteger.class, Contracts.REWARD_WEIGHT_CONTROLLER, "getAssetWeight", asset);
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getAllAssetDistributionPercentage(BigInteger)}
     */
    @Deprecated
    @External(readonly = true)
    public Map<String, ?> allAssetDistPercentage() {
        return call(Map.class, Contracts.REWARD_WEIGHT_CONTROLLER, "getAllAssetDistributionPercentage");
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getDistPercentageOfLP(BigInteger)}
     */
    @Deprecated
    @External(readonly = true)
    public Map<String, Map<String, BigInteger>> distPercentageOfAllLP() {
        return call(Map.class, Contracts.REWARD_WEIGHT_CONTROLLER, "getDistPercentageOfLP");
    }

    @External(readonly = true)
    public Map<String, BigInteger> getLiquidityProviders() {
        return this.assets.getLiquidityProviders();
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#setAssetWeight(String,
     * WeightStruct[], BigInteger)}}
     */
    @Deprecated
    @External
    public void configureAssetConfigs(AssetConfig[] _assetConfig) {
        Context.println("configureAssetConfigs-called" + Context.getCaller());
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#setAssetWeight(String,
     * WeightStruct[], BigInteger)}}
     */
    @Deprecated
    @External
    public void configureAssetConfig(AssetConfig _assetConfig) {
        Context.println("configureAssetConfig-called" + Context.getCaller());
    }

    @Override
    @Deprecated
    @External
    public void removeAssetConfig(Address _asset) {
        Context.println("removeAssetConfig-called" + Context.getCaller());
    }

    @Override
    @Deprecated
    @External
    public void updateEmissionPerSecond() {
        Context.println("updateEmissionPerSecond-called" + Context.getCaller());
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#tokenDistributionPerDay(BigInteger)}
     * BigInteger)}}
     */
    @Deprecated
    @External(readonly = true)
    public BigInteger tokenDistributionPerDay(BigInteger _day) {
        return call(BigInteger.class, Contracts.REWARD_WEIGHT_CONTROLLER, "tokenDistributionPerDay", _day);
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getDay()}
     */
    @Deprecated
    @External(readonly = true)
    public BigInteger getDay() {
        return call(BigInteger.class, Contracts.REWARD_WEIGHT_CONTROLLER, "getDay");
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getStartTimestamp()}
     * BigInteger)}}
     */
    @Deprecated
    @Override
    @External(readonly = true)
    public BigInteger getStartTimestamp() {
        return call(BigInteger.class, Contracts.REWARD_WEIGHT_CONTROLLER, "getStartTimestamp");
    }

    @Override
    @External(readonly = true)
    public BigInteger getPoolIDByAsset(Address _asset) {
        return this.assets.getPoolIDByAddress(_asset);
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getTypes()}
     */
    @Deprecated
    @External(readonly = true)
    public String[] getRecipients() {
        return call(String[].class, Contracts.REWARD_WEIGHT_CONTROLLER, "getTypes");
    }


    @External()
    public void disableRewardClaim() {
        checkGovernance();
        _isRewardClaimEnabled.set(Boolean.FALSE);
    }

    @External()
    public void enableRewardClaim() {
        checkGovernance();
        _isRewardClaimEnabled.set(Boolean.FALSE);
    }

    //    @Override
    @External(readonly = true)
    public boolean isRewardClaimEnabled() {
        return _isRewardClaimEnabled.get();
    }

    /**
     * @deprecated use {@link finance.omm.score.core.reward.RewardWeightControllerImpl#getDailyRewards(BigInteger)}
     */
    @Deprecated
    @Override
    @External(readonly = true)
    public Map<String, ?> getDailyRewards(@Optional BigInteger _day) {
        return call(Map.class, Contracts.REWARD_WEIGHT_CONTROLLER, "getDailyRewards", _day);
    }

    @Override
    @Deprecated
    @External
    public void startDistribution() {
        checkOwner();
        if (BigInteger.ZERO.equals(getDay()) && !_isInitialized.getOrDefault(false)) {
            _isInitialized.set(Boolean.TRUE);
        }
    }


    @Override
    public void distribute() {
        BigInteger day = distributedDay.getOrDefault(BigInteger.ZERO);  //0

        @SuppressWarnings("unchecked")
        Class<Map<String, ?>> clazz = (Class) Map.class;
        Map<String, ?> distributionInfo = call(clazz, Contracts.REWARD_WEIGHT_CONTROLLER, "distributionInfo", day);
        Boolean isValid = (Boolean) distributionInfo.get("isValid");

        if (!isValid) {
            return;
        }
        BigInteger tokenDistribution = (BigInteger) distributionInfo.get("distribution");
        BigInteger newDay = (BigInteger) distributionInfo.get("day");
        if (tokenDistribution.equals(BigInteger.ZERO)) {
            return;
        }
        distributedDay.set(newDay);
        call(Contracts.OMM_TOKEN, "mint", tokenDistribution);
        OmmTokenMinted(newDay, tokenDistribution, newDay.subtract(day));

        BigInteger transferToContract = BigInteger.ZERO;

        for (Address key : this.transferToContractMap.keySet()) {
            BigInteger oldIndex = this.assets.getAssetIndex(key);
            BigInteger newIndex = this.getAssetIndex(key, ICX, false);
            BigInteger accruedRewards = calculateReward(ICX, newIndex, oldIndex);
            transferToContract = transferToContract.add(accruedRewards);

            if (Contracts.WORKER_TOKEN.getKey().equals(transferToContractMap.get(key))) {
                distributeWorkerToken(accruedRewards);
            } else if (Contracts.DAO_FUND.getKey().equals(transferToContractMap.get(key))) {
                call(Contracts.OMM_TOKEN, "transfer", Contracts.DAO_FUND, accruedRewards);
                Distribution("daoFund", getAddress(Contracts.DAO_FUND.toString()), accruedRewards);
            }
        }

        if (transferToContract.compareTo(tokenDistribution) > 0) {
            throw RewardDistributionException.unknown("transfer to contract exceed total distribution");
        }

    }

    /**
     * @param reward - BigInteger
     * @deprecated use tokenFallback to distribute token to workerToken holders
     */
    @Deprecated
    private void distributeWorkerToken(BigInteger reward) {
        Address[] walletHolders = call(Address[].class, Contracts.WORKER_TOKEN, "getWallets");
        BigInteger totalSupply = call(BigInteger.class, Contracts.WORKER_TOKEN, "totalSupply");
        BigInteger total = BigInteger.ZERO;
        for (Address user : walletHolders) {
            BigInteger userWorkerTokenBalance = call(BigInteger.class, Contracts.WORKER_TOKEN, "balanceOf", user);
            BigInteger amount = MathUtils.exaMultiply(MathUtils.exaDivide(userWorkerTokenBalance, totalSupply), reward);
            Distribution("worker", user, amount);
            call(Contracts.OMM_TOKEN, "transfer", user, amount);
            totalSupply = totalSupply.subtract(userWorkerTokenBalance);
            total = total.add(amount);
        }
        if (total.compareTo(reward) > 0) {
            throw RewardDistributionException.unknown("worker token distribution exceed accrued reward");
        }
    }


    @Override
    @External(readonly = true)
    public BigInteger getDistributedDay() {
        return this.distributedDay.get();
    }

    @Override
    public void transferOmmToDaoFund(BigInteger _value) {
        checkGovernance();
        Address daoFundAddress = this.getAddress(Contracts.DAO_FUND.getKey());
        call(Contracts.OMM_TOKEN, "transfer", daoFundAddress, _value);
    }

    @Override
    public void tokenFallback(Address _from, BigInteger _value, byte[] _data) {

    }

    @External(readonly = true)
    public Map<String, BigInteger> getUserDailyReward(Address user) {
        Map<String, String> assets = this.assets.getAssetName(this.transferToContractMap.keySet());

        Map<String, BigInteger> dailyRewards = call(Map.class, Contracts.REWARD_WEIGHT_CONTROLLER,
                "getAssetDailyRewards");

        DictDB<Address, BigInteger> balances = workingBalance.at(user);
        Map<String, BigInteger> response = new HashMap<>();
        for (Map.Entry<String, String> entry : assets.entrySet()) {
            Address assetAddr = Address.fromString(entry.getKey());
            String name = entry.getValue();
            BigInteger userWorkingBalance = balances.getOrDefault(assetAddr, BigInteger.ZERO);
            BigInteger assetWorkingTotal = workingTotal.getOrDefault(assetAddr, BigInteger.ZERO);
            BigInteger dailyReward = dailyRewards.get(name);

            response.put(name, exaMultiply(dailyReward, exaDivide(userWorkingBalance, assetWorkingTotal)));
        }
        return response;

    }

    @Override
    @External()
    public void handleAction(UserDetails _userAssetDetails) {
        Address _asset = Context.getCaller();
        _handleAction(_asset, _userAssetDetails);
    }

    @Override
    @External
    public void handleLPAction(Address _asset, UserDetails _userDetails) {
        checkStakeLp();
        _handleAction(_asset, _userDetails);
    }

    private void _handleAction(Address _asset, UserDetails _userDetails) {
        Asset asset = this.assets.get(_asset);
        if (asset == null) {
            throw RewardDistributionException.invalidAsset("Asset is null (" + _asset + ")");
        }
        BigInteger _decimals = _userDetails._decimals;
        Address _user = _userDetails._user;
        BigInteger _userBalance = MathUtils.convertToExa(_userDetails._userBalance, _decimals);
        BigInteger _totalSupply = MathUtils.convertToExa(_userDetails._totalSupply, _decimals);

        updateWorkingBalance(_asset, _user, _userBalance, _totalSupply);
    }

    @EventLog()
    public void OmmTokenMinted(BigInteger _day, BigInteger _value, BigInteger _days) {}

    @EventLog(indexed = 2)
    public void Distribution(String _recipient, Address _user, BigInteger _value) {}

}
