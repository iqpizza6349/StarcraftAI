import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Flag.Enum;
import bwta.BWTA;

public class Main3 extends DefaultBWListener {

	Mirror mirror = new Mirror();
	Game game;
	Player player;
	
	public boolean buildFin = false;
	
	public static void main(String[] args) {
		try {
			new Main3().run();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void run() {
		// TODO Auto-generated method stub
		mirror.getModule().setEventListener(this);
		
		mirror.startGame();
	}
	
	@Override
	public void onEnd(boolean isWin) {
		if(isWin) {
			System.out.println("I won the game");
		} else {
			System.out.println("I lost the game");
		}
		
		System.exit(0);
	}
	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		game = mirror.getGame();
		player = game.self();
		
		game.enableFlag(Enum.UserInput.getValue());
		
		game.setLocalSpeed(0);
		game.setFrameSkip(0);
		
		BWTA.analyze();
		BWTA.readMap();
		
		
	}
	
	private void constructBuilding(UnitType Buildingtype) {
		Unit producer = null;
		UnitType producerUnitType = Buildingtype.whatBuilds().first;
		
		for (Unit unit : player.getUnits()) {
			if (unit.getType() == producerUnitType) {
				if (game.canMake(Buildingtype, unit)
						&& unit.isCompleted()
						&& !unit.isCarryingMinerals()
						&& !unit.isConstructing()
						&& !unit.isGatheringMinerals()
						&& !unit.isCarryingGas()
						&& !unit.isGatheringGas()
						&& unit.isIdle()) {
					producer = unit;
					break;
				}
			}
		}
		
		if (producer == null) {
			return;
		}
		
		TilePosition seedPosition = player.getStartLocation();
		TilePosition desired = TilePosition.None;
		int max = 32;
		boolean constructionPlaceFound = false;
		
		for (int range = 8; range <= max; range *= 2) {
			for (int i = seedPosition.getX() - range; i < seedPosition.getX() + range; i++) {
				for (int j = seedPosition.getY() - range; j < seedPosition.getY() +range; j++) {
					desired = new TilePosition(i, j);
					if (game.canBuildHere(desired, Buildingtype, producer, true)) {
						constructionPlaceFound = true;
						break;
					}
				}
				if (constructionPlaceFound) break;
			}
			if (constructionPlaceFound) break;
		}
		if (constructionPlaceFound == true && desired != TilePosition.None) {
			producer.build(Buildingtype, desired);
		}
	}
	

	
	@Override
	public void onFrame() {
		// TODO Auto-generated method stub
		
		for (Unit myUnit : player.getUnits()) {
			
			if (myUnit.canTrain(UnitType.Terran_SCV) && !myUnit.canCancelTrain()) {
				myUnit.train(UnitType.Terran_SCV);
			}
			
			if (player.supplyTotal() == player.supplyUsed()) {
				if (player.supplyTotal() < 60)
				constructBuilding(UnitType.Terran_Supply_Depot);
			}
			
			if (player.minerals() > 5000) {
				System.out.println("5000미네랄 성공함");
				System.exit(0);
			}
			
			if (myUnit.getType().isWorker() && myUnit.isIdle()) {
				Unit closestMinerial = null;
				
				for (Unit minerial : game.neutral().getUnits()) {
					if (minerial.getType().isMineralField()) {
						if (closestMinerial == null || myUnit.getDistance(minerial) < myUnit.getDistance(closestMinerial)) {
							closestMinerial = minerial;
						}
					}
					if (closestMinerial != null) {
						myUnit.gather(closestMinerial);
					}
				}
				
			}
			
		}
	}
	
}
